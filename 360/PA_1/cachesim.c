#include <getopt.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <math.h>
#include <errno.h>
// other headers as needed
#include <string.h>
#include <libgen.h>

#define ADDRESS_LENGTH 64  // 64-bit memory addressing

// other variables as needed

/* -------------------- ENUMS -------------------- */

// typdef for a line in our cache
typedef struct {
    int valid;
    unsigned long tag;   // addresses are 64-bit
    unsigned long lru;   // e.g., a global access counter
} Line;

// typedef for type of access
typedef enum {
    LOAD,
    STORE,
    MODIFY
} AccessType;

// typedef for the type of access result
typedef enum {
    HIT,        // found the data, no problems
    MISS,       // didn't find data, had to fill in empty line
    EVICT,      // didn't fine data, had to replace old line (is also a miss)
    NONE,       // for an optional second param in the output func
} AccessResult;

static const char *resultStr(AccessResult r) {
    switch (r) {
        case HIT:   return "hit";
        case MISS:  return "miss";
        case EVICT: return "miss eviction";
        default:    return "";
    }
}

/* -------------------- Given Functions -------------------- */

/* 
 * this function provides a standard way for your cache
 * simulator to display its final statistics (i.e., hit and miss)
 */ 
void print_summary(int hits, int misses, int evictions)
{
    printf("hits:%d misses:%d evictions:%d\n", hits, misses, evictions);
}

/*
 * print usage info
 */
void print_usage(char* argv[])
{
    printf("Usage: %s [-hv] -s <num> -E <num> -b <num> -t <file>\n", argv[0]);
    printf("Options:\n");
    printf("  -h         Print this help message.\n");
    printf("  -v         Optional verbose flag.\n");
    printf("  -s <num>   Number of set index bits.\n");
    printf("  -E <num>   Number of lines per set.\n");
    printf("  -b <num>   Number of block offset bits.\n");
    printf("  -t <file>  Trace file.\n");
    printf("\nExamples:\n");
    printf("  linux>  %s -s 4 -E 1 -b 4 -t traces/trace01.dat\n", argv[0]);
    printf("  linux>  %s -v -s 8 -E 2 -b 4 -t traces/trace01.dat\n", argv[0]);
    exit(0);
}

/* -------------------- String Manipulation -------------------- */

/*
 * Function to create outfile name string from input file
 * Could be more modular and derive output directory dynamically based on what's passed in,
 *   but does what it needs for this assignment
 * This extracts the basename, adds a hardcoded "traces/output" in front and "out.txt" at back
 *
 * Example: "traces/trace02.dat" --> "traces/output/trace02out.txt"
 *           --------------xxxx       -------^^^^^^^-------+++++++
*/
char *make_outname(const char *tracefileName) {
    // make sure tracefile exists
    if (tracefileName == NULL) {
        perror("Tracefile name was not found");
        return NULL;
    }

    // make a copy of the trace file name, since I don't want basename to modify it
    char *tracefileCopy = strdup(tracefileName);
    if (tracefileCopy == NULL) {
        perror("Not enough memory to allocate copy of file name!");
        return NULL;
    }

    // get the base name of the file
    char *name = basename(tracefileCopy);  // for example traces/trace02.dat --> trace02.dat

    // get the position of the last dot char in the file name
    char *dot = strrchr(name, '.');

    // truncate at the dot to remove the extension, if it exists
    if (dot) *dot = '\0';  // for example trace02.dat --> trace02

    // calculate the length needed for the new string
    size_t size = strlen("traces/output/") + strlen(name) + strlen("out.txt") + 1;
    
    // try to create memory for new string, return on fail
    char *outfileName = malloc(size);
    if (outfileName == NULL) {
        perror("Not enough memory to allocate outfile string!");
        free(tracefileCopy);
        return NULL;
    }

    // fill string with new name
    snprintf(outfileName, size, "%s%sout.txt", "traces/output/", name);

    // free copied name string and return string ptr to new name
    free(tracefileCopy);
    return outfileName;
}

/* -------------------- File Functions -------------------- */

/*
 * Open the tracefile for reading
 *
 * Takes a c string like "traces/trace02.dat"
 * Can be any form like "traces/trace#...#.dat" where #...# is any number of digits [0-9]
*/
FILE *openReadTracefile(const char *tracefileName) {
    return fopen(tracefileName, "r");
}

/*
 * Open the output tracefile for writing
 *
 * Takes a c string like "traces/trace02.dat"
 * Can be any form like "traces/trace#...#.dat" where #...# is any number of digits [0-9]
 *
 * Creates a file at "traces/output/trace#...#out.txt", where #...# is the same number as above ^
*/
FILE *openWriteTracefile(const char *tracefileName) {
    // get output file name, return if fail
    char *outfileName = make_outname(tracefileName);
    if (outfileName == NULL) return NULL;
    
    // open file, return if fail
    FILE *fp = fopen(make_outname(tracefileName), "w");
    if (fp == NULL) return NULL;

    // free output file name string and return file poitner
    free(outfileName);
    return fp;
}

/*
 * Write details of trace to file
 *
 * AccessResult r1 should be a HIT, MISS, or EVICT always.
 * AccessResult r2 may be NONE for L and S instructions, but can be anything else for a M instruction
*/
void writeTraceDetails(FILE *outfile, char op, unsigned long int address, int size, AccessResult r1, AccessResult r2) {
    // instruction part
    fprintf(outfile, "%c %lx,%d ", op, address, size);

    // required first result part
    fprintf(outfile, "%s", resultStr(r1));

    // optional second result part (for modify ops)
    if (r2 != NONE) {
        fprintf(outfile, " %s", resultStr(r2));
    }

    fputc('\n', outfile);
}

/* 
 * this function provides a standard way for your cache
 * simulator to display its final statistics (i.e., hit and miss)
 */ 
void writeSummary(FILE *outfile, int hits, int misses, int evictions)
{
    fprintf(outfile, "hits:%d misses:%d evictions:%d\n", hits, misses, evictions);
}

/* -------------------- Cache Functions -------------------- */

/*
 * Function to allocate memory for the simulated cache based on input parameters
 *
 * Parameters:
 *  - s: bits for number of sets (S = 2^s)
 *  - E: number of lines per set
*/
Line* createCache(int s, int E) {
    size_t S = pow(2, s);
    Line* cache = calloc(S * E, sizeof(Line));

    if (cache == NULL)
        perror("Unable to allocate enough memory for the cache");
    
    return cache;
}

/*
 * Access a set and do an action
 *
 * Parameters:
 *  - *cache: the array of Line structs, which represents the cache
 *  - set: the set # we're looking to access [0..S]
 *  - E: the number of lines per set [1+]
 *  - tag: the tag to look for/set
 *  - *accessCounter: the global counter used for tracking LRU lines, pointer for access
 *
 * Returns:
 *  - AccessResult type, like HIT, MISS, or EVICT (which is also a miss)
*/
AccessResult accessSet(Line* cache, unsigned int set, int E, unsigned long tag, unsigned long *accessCounter) {
    // NOTE: could simplify the cache[set*E + i] part to something like setLines[i] somehow

    // step 1: try to find a line with valid bit and matching tag
    for (int i = 0; i < E; i++) {
        if (cache[set*E + i].valid == 1 && cache[set*E + i].tag == tag) {
            cache[set*E + i].lru = ++(*accessCounter);
            return HIT;
        }
    }

    // step 2: try to find an empty line to fill & use
    for (int i = 0; i < E; i++) {
        if (cache[set*E + i].valid == 0) {
            cache[set*E + i].valid = 1;
            cache[set*E + i].tag = tag;
            cache[set*E + i].lru = ++(*accessCounter);
            return MISS;
        }
    }

    // step 3: try to find the least recently used line to evict
    unsigned int lruLine = 0;
    unsigned int lruCounter = *accessCounter;
    for (int i = 0; i < E; i++) {
        if (cache[set*E + i].lru < lruCounter) {
            lruCounter = cache[set*E + i].lru;
            lruLine = i;
        }
    }

    // ... then evict line
    cache[set*E + lruLine].tag = tag;
    cache[set*E + lruLine].lru = ++(*accessCounter);

    return EVICT;
}

/*
 * Function to update hit, miss, and evictions counters
 * Really just an switch abstraction
 *
 * Parameters:
 *  - res: access result type
 *  - *hits: counter to modify
 *  - *misses: counter to modify
 *  - *evictions: counter to modify
*/
void updateCounters(AccessResult res, int *hits, int *misses, int *evictions) {
    switch (res) {
        case HIT:
            (*hits)++;
            break;
        case EVICT:
            (*evictions)++;  // continues to MISS...
            /* fall through */
        case MISS:
            (*misses)++;
            break;
        case NONE:
        default:
            break;
    }
}

/*
 * Cache simulation loop
 *  - Reads each line of the input trace file
 *  - Discards any instruction loads
 *  - Performs accesses on the cache based in access type (like L, S, or M)
 *  - Increments count of hits, misses, and evicts
 *  - Write trace to output file
 *
 * Parameters:
 *  - cache: Array of Line structs representing the cache
 *  - s: bits for number of sets
 *  - E: number of lines per set
 *  - b: bits for number of blocks
 *  - *accessCounter: global counter for tracking LRU lines, pointer for access
 *  - *hits: pointer to hits counter
 *  - *misses:   ""     misses counter
 *  - *evictions:  ""   evictions counter
 *  - *infile: File to read from
 *  - *outfile: File to output to
 *  - isVerbose: whether (1) or not (0) verbose mode is enabled
 *
 * Returns:
 *  - number of successful lines read
*/
int cacheSimLoop(Line* cache, int s, int E, int b, unsigned long *accessCounter, int *hits, int *misses, int *evictions, FILE *infile, FILE *outfile, int isVerbose) {
    // BASE CONDITION JUST IN CASE:
    if (cache == NULL || accessCounter == NULL || hits == NULL || misses == NULL || evictions == NULL || infile == NULL || outfile == NULL) {
        fprintf(stderr, "ERROR: One of the parameters passed into cacheSimLoop was left NULL!");
        return -1;
    }
    
    int linesRead = 0;
    char line[48] = "";  // 48 characters should be enough, even for like 20 hex digits in the address cus why not

    // read file loop
    while (fgets(line, sizeof(line), infile) != NULL) {
        // skip on instruction load line
        if (line[0] == 'I')
            continue;

        // variables for the current line
        char op;                    // i.e. L/S/M
        unsigned long int address;  // i.e. 5b or 7fefe0588
        int size;                   // i.e. 4 (unused)

        // scan and parse the line to get operator, address, and size
        if (sscanf(line, " %c %lx,%d", &op, &address, &size) != 3)
            continue;  // malformed, continue onto the next line

        // calc set num and tag num
        unsigned int set = (address >> b) & ((1UL << s) - 1);
        unsigned long tag = address >> (s + b);
        // we would also get offset but we're not dealing with that here

        AccessResult r1 = NONE, r2 = NONE;

        // based on operator, access the cache and write the result out to the outfile
        switch (op) {
            case 'S':  // fall-through to L since they're the same case
            case 'L':
                r1 = accessSet(cache, set, E, tag, accessCounter);
                writeTraceDetails(outfile, op, address, size, r1, NONE);
                if (isVerbose) writeTraceDetails(stdout, op, address, size, r1, NONE);
                updateCounters(r1, hits, misses, evictions);
                break;
            case 'M':
                r1 = accessSet(cache, set, E, tag, accessCounter);  // first: try load
                r2 = accessSet(cache, set, E, tag, accessCounter);  // second: store (should always hit)
                updateCounters(r1, hits, misses, evictions);
                updateCounters(r2, hits, misses, evictions);
                writeTraceDetails(outfile, op, address, size, r1, r2);
                if (isVerbose) writeTraceDetails(stdout, op, address, size, r1, r2);
                break;
            default:
                continue;
        }

        // inc lines read for fun
        linesRead++;
    }

    return linesRead;
}

/* -------------------- Main -------------------- */

/*
 * starting point
 */
int main(int argc, char* argv[])
{
    // count variables
    int hit_count = 0, miss_count = 0, eviction_count = 0;

    // runtime cache variables
    int setIndexBits = -1, linesPerSet = -1, blockBits = -1;  // more descriptive names in order of s, E, b
    unsigned long accessCounter = 0;                          // global access counter for tracking LRU lines
    char *tracefileName = NULL;                               // string to tracefile name
    FILE* infile = NULL, *outfile = NULL;                     // input and output trace files
    Line* cache = NULL;                                       // cache array
    int isVerbose = 0;

	// completed my simulator :)
    int c = 0;
    while( (c=getopt(argc,argv,"s:E:b:t:vh")) != -1){
        switch(c){
            case 's':
                setIndexBits = atoi(optarg);
                break;
            case 'E':
                linesPerSet = atoi(optarg);
                break;
            case 'b':
                blockBits = atoi(optarg);
                break;
            case 't':
                tracefileName = optarg;
                break;
            case 'v':
                isVerbose = 1;
                break;
            case 'h':
                print_usage(argv);
                exit(0);
            default:
                print_usage(argv);
                exit(1);
        }
    }

    // post-argument parsing check (make sure required arguments were provided)
    if (setIndexBits < 0 || linesPerSet <= 0 || blockBits < 0 || tracefileName == NULL) {
        print_usage(argv);
        exit(1);
    }

    // try open files
    infile = openReadTracefile(tracefileName);

    if (infile == NULL) {
        perror("Input trace file does not exist!");
        exit(1);
    }

    outfile = openWriteTracefile(tracefileName);

    if (outfile == NULL) {
        perror("Could not create the output trace file!");
        exit(1);
    }

    // set up cache
    cache = createCache(setIndexBits, linesPerSet);

    // scary -- run main loop!
    // AKA run the simulator
    cacheSimLoop(cache, setIndexBits, linesPerSet, blockBits, &accessCounter, &hit_count, &miss_count, &eviction_count, infile, outfile, isVerbose);

    // output cache hit and miss statistics
    print_summary(hit_count, miss_count, eviction_count);
    writeSummary(outfile, hit_count, miss_count, eviction_count);
    
    // close open files and dynamic memory
    fclose(infile);
    fclose(outfile);
    free(cache);

    // assignment done. life is good! <- love it! :D
    return 0;
}
