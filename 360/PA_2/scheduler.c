#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>

// Headers as needed

typedef enum {false, true} bool;        // Allows boolean types in C

typedef enum {unstarted, ready, running, blocked, terminated} _proc_state;

typedef enum {FCFS, RR, SJF} scheduling_technique;

/* Defines a job struct */
typedef struct Process {
    uint32_t A;                     // A: Arrival time of the process
    uint32_t B;                     // B: Upper Bound of CPU burst times of the given random integer list
    uint32_t C;                     // C: Total CPU time required
    uint32_t M;                     // M: Multiplier of CPU burst time
    uint32_t processID;             // The process ID given upon input read

    // Add other fields as needed
    _proc_state state;
    uint32_t remainingCPUTime;
    uint32_t remainingBurst;        // the CPU cycles left in the current burst.
    uint32_t burstLength;           // the full length of the current burst
    uint32_t remainingIO;           // io cycles left in current block
    uint32_t quantumLeft;           // only for round robin
    uint32_t finishingTime;         // cpu cycle number process finished at
    uint32_t currentIOBlockedTime;
    uint32_t currentWaitingTime;    // cycles the process spent waiting
    uint32_t currentCPUTimeRun;
    bool newlyReady;                // should the process be considered for being put into the queue?
} _process;

uint32_t TOTAL_CREATED_PROCESSES = 0;   // The total number of processes constructed
const uint32_t SEED_VALUE = 200;  // Seed value for reading from file

// Additional variables as needed
uint32_t TOTAL_FINISHED_PROCESSES = 0;
uint32_t CURRENT_CYCLE = 0;
uint32_t TOTAL_NUMBER_OF_CYCLES_SPENT_BLOCKED = 0;

// simple queue array implementation for FCFS and RR (not circular; dequeue shifts, O(n))
uint32_t *queue;
uint32_t count = 0;

void enqueue(uint32_t idx) {
    if (count >= TOTAL_CREATED_PROCESSES) {
        fprintf(stderr, "Queue error: enqueue on full queue. Is a process being put in more than once?\n");
        return;
    }
    queue[count++] = idx;               // add to the back
}

int32_t peek() {
    if (count == 0) return -1;
    return (int32_t) queue[0];          // front of the queue
}

int32_t dequeue() {
    if (count == 0) {
        fprintf(stderr, "Queue error: dequeue from empty queue\n");
        return -1;
    }

    int32_t ret = (int32_t) queue[0];

    for (uint32_t i = 0; i + 1 < count; i++)   // shift everything left by one
        queue[i] = queue[i + 1];
    count--;

    return ret;
}

/**
 * Reads a random non-negative integer X from a file with a given line named random-numbers (in the current directory)
 */
uint32_t getRandNumFromFile(uint32_t line, FILE* random_num_file_ptr){
    uint32_t end, loop;
    char str[512];

    rewind(random_num_file_ptr); // reset to be beginning
    for(end = loop = 0;loop<line;++loop){
        if(0==fgets(str, sizeof(str), random_num_file_ptr)){ //include '\n'
            end = 1;  //can't input (EOF)
            break;
        }
    }
    if(!end) {
        return (uint32_t) atoi(str);
    }

    // Fail-safe return
    return (uint32_t) 1804289383;
}

/**
 * Reads a random non-negative integer X from a file named random-numbers.
 * Returns the CPU Burst: : 1 + (random-number-from-file % upper_bound)
 */
uint32_t randomOS(uint32_t upper_bound, uint32_t process_indx, FILE* random_num_file_ptr)
{
    char str[20];
    
    uint32_t unsigned_rand_int = (uint32_t) getRandNumFromFile(SEED_VALUE+process_indx, random_num_file_ptr);
    uint32_t returnValue = 1 + (unsigned_rand_int % upper_bound);

    return returnValue;
} 

/********************* SOME PRINTING HELPERS *********************/

/**
 * Prints to standard output the original input
 * process_list is the original processes inputted (in array form)
 */
void printStart(_process process_list[])
{
    printf("The original input was: %i", TOTAL_CREATED_PROCESSES);

    uint32_t i = 0;
    for (; i < TOTAL_CREATED_PROCESSES; ++i)
    {
        printf(" ( %i %i %i %i)", process_list[i].A, process_list[i].B,
               process_list[i].C, process_list[i].M);
    }
    printf("\n");
} 

/**
 * Prints to standard output the final output
 * finished_process_list is the terminated processes (in array form) in the order they each finished in.
 */
void printFinal(_process finished_process_list[])
{
    printf("The (sorted) input is: %i", TOTAL_CREATED_PROCESSES);

    uint32_t i = 0;
    for (; i < TOTAL_FINISHED_PROCESSES; ++i)
    {
        printf(" ( %i %i %i %i)", finished_process_list[i].A, finished_process_list[i].B,
               finished_process_list[i].C, finished_process_list[i].M);
    }
    printf("\n");
} // End of the print final function

/**
 * Prints out specifics for each process  (helper function, you may need to adjust variables accordingly)
 * @param process_list The original processes inputted, in array form
 */
void printProcessSpecifics(_process process_list[])
{
    uint32_t i = 0;
    printf("\n");
    for (; i < TOTAL_CREATED_PROCESSES; ++i)
    {
        printf("Process %i:\n", process_list[i].processID);
        printf("\t(A,B,C,M) = (%i,%i,%i,%i)\n", process_list[i].A, process_list[i].B,
               process_list[i].C, process_list[i].M);
        printf("\tFinishing time: %i\n", process_list[i].finishingTime);
        printf("\tTurnaround time: %i\n", process_list[i].finishingTime - process_list[i].A);
        printf("\tI/O time: %i\n", process_list[i].currentIOBlockedTime);
        printf("\tWaiting time: %i\n", process_list[i].currentWaitingTime);
        printf("\n");
    }
} // End of the print process specifics function

/**
 * Prints out the summary data (helper function, you may need to adjust variables accordingly)
 * process_list The original processes inputted in array form
 */
void printSummaryData(_process process_list[])
{
    uint32_t i = 0;
    double total_amount_of_time_utilizing_cpu = 0.0;
    double total_amount_of_time_io_blocked = 0.0;
    double total_amount_of_time_spent_waiting = 0.0;
    double total_turnaround_time = 0.0;
    uint32_t final_finishing_time = CURRENT_CYCLE - 1;
    for (; i < TOTAL_CREATED_PROCESSES; ++i)
    {
        total_amount_of_time_utilizing_cpu += process_list[i].currentCPUTimeRun;
        total_amount_of_time_io_blocked += process_list[i].currentIOBlockedTime;
        total_amount_of_time_spent_waiting += process_list[i].currentWaitingTime;
        total_turnaround_time += (process_list[i].finishingTime - process_list[i].A);
    }

    // Calculates the CPU utilisation
    double cpu_util = total_amount_of_time_utilizing_cpu / final_finishing_time;

    // Calculates the IO utilisation
    double io_util = (double) TOTAL_NUMBER_OF_CYCLES_SPENT_BLOCKED / final_finishing_time;

    // Calculates the throughput (Number of processes over the final finishing time times 100)
    double throughput =  100 * ((double) TOTAL_CREATED_PROCESSES/ final_finishing_time);

    // Calculates the average turnaround time
    double avg_turnaround_time = total_turnaround_time / TOTAL_CREATED_PROCESSES;

    // Calculates the average waiting time
    double avg_waiting_time = total_amount_of_time_spent_waiting / TOTAL_CREATED_PROCESSES;

    printf("Summary Data:\n");
    printf("\tFinishing time: %i\n", CURRENT_CYCLE - 1);
    printf("\tCPU Utilisation: %6f\n", cpu_util);
    printf("\tI/O Utilisation: %6f\n", io_util);
    printf("\tThroughput: %6f processes per hundred cycles\n", throughput);
    printf("\tAverage turnaround time: %6f\n", avg_turnaround_time);
    printf("\tAverage waiting time: %6f\n", avg_waiting_time);
} // End of the print summary data function

/********************* IMPLEMENTATION FILE HELPERS *********************/

// All this does is read the first number and return it...
uint32_t get_num_processes(FILE *processes_file) {
    uint32_t num = 0;

    fscanf(processes_file, "%u", &num);

    return num;
}

/********************* IMPLEMENTATION PROCESS HELPERS *********************/

void populate_processes_from_file(FILE *processes_file, _process *proc_list, uint32_t num_processes) {
    
    for (uint32_t i = 0; i < num_processes; i++) {  // read until no more process tuples (# # # #) are left
        fscanf(
            processes_file,
            " (%u %u %u %u)",     // format
            &(proc_list[i].A),    // arrival time
            &(proc_list[i].B),    // CPU bursts
            &(proc_list[i].C),    // total cpu time needed
            &(proc_list[i].M)     // IO bursts
        );
    }
}

// CLAUDE AI: "Create a function with the given function header to copy all processes into a new list and sort via insertion sort."
void sort_processes_into_list(_process *unsorted_list, _process *sorted_list)
{
    uint32_t n = TOTAL_CREATED_PROCESSES;

    // Copy the original input so the unsorted list stays intact for printStart()
    for (uint32_t i = 0; i < n; ++i)
        sorted_list[i] = unsorted_list[i];

    // Stable insertion sort by arrival time; equal A values keep input order
    for (uint32_t i = 1; i < n; ++i) {
        _process key = sorted_list[i];
        int32_t j = (int32_t) i - 1;
        while (j >= 0 && sorted_list[j].A > key.A) {   // strict > keeps it stable
            sorted_list[j + 1] = sorted_list[j];
            --j;
        }
        sorted_list[j + 1] = key;
    }

    // IDs follow sorted order: 0 .. n-1
    for (uint32_t i = 0; i < n; ++i)
        sorted_list[i].processID = i;
}

/********************* SIMULATION FUNCTION / HELPERS *********************/

void simulate(_process *original_list, _process *sorted_list, scheduling_technique technique, FILE *random_num_file) {
    // copy processes into working array, keeping original untouched
    _process *processes = calloc(TOTAL_CREATED_PROCESSES, sizeof(_process));
    memcpy(processes, sorted_list, sizeof(_process) * TOTAL_CREATED_PROCESSES);

    // reset every process
    for (uint32_t i = 0; i < TOTAL_CREATED_PROCESSES; i++) {
        processes[i].state = unstarted;
        processes[i].remainingCPUTime = processes[i].C;
        processes[i].remainingBurst = 0;
        processes[i].remainingIO = 0;
        processes[i].quantumLeft = 0;
        processes[i].finishingTime = 0;
        processes[i].currentIOBlockedTime = 0;
        processes[i].currentWaitingTime = 0;
        processes[i].currentCPUTimeRun = 0;
        processes[i].newlyReady = false;
    }

    // reset globals
    CURRENT_CYCLE = 0;
    TOTAL_FINISHED_PROCESSES = 0;
    TOTAL_NUMBER_OF_CYCLES_SPENT_BLOCKED = 0;

    // running flag/index
    int32_t running_idx = -1;

    // reset queue count to 0 (ignore dequeueing because data will be overwritten as needed)
    count = 0;

    // cycle loop
    while (TOTAL_FINISHED_PROCESSES < TOTAL_CREATED_PROCESSES) {
        // 1. account for a cycle passing, update processes
        if (CURRENT_CYCLE > 0) {
            // update running cycle
            if (running_idx != -1) {
                processes[running_idx].currentCPUTimeRun++;
                processes[running_idx].remainingCPUTime--;
                processes[running_idx].remainingBurst--;
                if (technique == RR) processes[running_idx].quantumLeft--;
            }

            // update other processes blocked & ready
            int atLeastOneBlocked = 0;
            for (uint32_t i = 0; i < TOTAL_CREATED_PROCESSES; i++) {
                // blocked process
                if (processes[i].state == blocked) {
                    processes[i].currentIOBlockedTime++;
                    processes[i].remainingIO--;
                    
                    atLeastOneBlocked = 1;
                }

                // ready process
                if (processes[i].state == ready) {
                    processes[i].currentWaitingTime++;
                }
            }

            if (atLeastOneBlocked) TOTAL_NUMBER_OF_CYCLES_SPENT_BLOCKED++;
        }

        // 2. running process transitions - checking in tie order
        if (running_idx != -1) {
            if (processes[running_idx].remainingCPUTime == 0) {  // process done
                processes[running_idx].finishingTime = CURRENT_CYCLE;
                processes[running_idx].state = terminated;
                TOTAL_FINISHED_PROCESSES++;
                running_idx = -1;
            }
            else if (processes[running_idx].remainingBurst == 0) {  // process blocked
                processes[running_idx].remainingIO = processes[running_idx].burstLength * processes[running_idx].M;
                processes[running_idx].state = blocked;
                running_idx = -1;
            }
            else if (technique == RR && processes[running_idx].quantumLeft == 0) {  // ready again for RR
                processes[running_idx].state = ready;
                processes[running_idx].newlyReady = true;
                running_idx = -1;
            }
        }

        // 3. process any processes going from blocked --> ready
        for (uint32_t i = 0; i < TOTAL_CREATED_PROCESSES; i++) {
            if (processes[i].state == blocked && processes[i].remainingIO == 0) {
                processes[i].state = ready;
                processes[i].newlyReady = true;
            }
        }

        // 4. new arrivals
        for (uint32_t i = 0; i < TOTAL_CREATED_PROCESSES; i++) {
            if (processes[i].state == unstarted && processes[i].A == CURRENT_CYCLE) {
                processes[i].state = ready;
                processes[i].newlyReady = true;
            }
        }

        // 5. enqueue processes for fcfs and rr in tie-break order
        if (technique != SJF) {
            for (uint32_t i = 0; i < TOTAL_CREATED_PROCESSES; i++) {
                if (processes[i].newlyReady) {
                    enqueue(i);  // part of tie-breaker here; marking newly ready and enqueueing in order
                }
            }
        }

        // clear all newly ready marks
        for (uint32_t i = 0; i < TOTAL_CREATED_PROCESSES; i++) {
            processes[i].newlyReady = false;
        }

        // 6. dispatch: only when the CPU is free
        if (running_idx == -1) {
            if (technique == FCFS || technique == RR) {
                if (count > 0) running_idx = dequeue();
            }
            else {  // SJF: smallest remaining total time; strict < keeps lowest ID on ties
                for (uint32_t i = 0; i < TOTAL_CREATED_PROCESSES; i++) {
                    if (processes[i].state != ready) continue;
                    if (running_idx == -1 ||
                        processes[i].remainingCPUTime < processes[running_idx].remainingCPUTime)
                        running_idx = (int32_t) i;
                }
            }

            // set up the newly dispatched process
            if (running_idx != -1) {
                _process *p = &processes[running_idx];
                p->state = running;

                if (p->remainingBurst == 0) {   // fresh burst (not resuming after RR preemption)
                    uint32_t burst = randomOS(p->B, p->processID, random_num_file);
                    if (burst > p->remainingCPUTime) burst = p->remainingCPUTime;
                    p->remainingBurst = p->burstLength = burst;
                }

                if (technique == RR) p->quantumLeft = 2;
            }
        }

        CURRENT_CYCLE++;
    }

    // after-loop printing
    printf("######################### START OF %s #########################\n",
        (technique == FCFS) ? "FIRST COME FIRST SERVE" :
        (technique == RR) ? "ROUND ROBIN" :
        "SHORTEST JOB FIRST"
    );

    printStart(original_list);
    printFinal(processes);

    printf("\nThe scheduling algorithm used was %s\n",
        (technique == FCFS) ? "First Come First Serve" :
        (technique == RR) ? "Round Robin" :
        "Shortest Job First"
    );

    printProcessSpecifics(processes);
    printSummaryData(processes);

    printf("######################### END OF %s #########################\n",
        (technique == FCFS) ? "FIRST COME FIRST SERVE" :
        (technique == RR) ? "ROUND ROBIN" :
        "SHORTEST JOB FIRST"
    );

    // clean up
    free(processes);
}

/********************* MAIN *********************/

/**
 * The magic starts from here
 */
int main(int argc, char *argv[])
{
    // Write code for your shiny scheduler

    // make sure input file was given
    if (argc < 2) {
        fprintf(stderr, "Usage: %s <input-file>\n", argv[0]);
        return 1;
    }

    // files
    FILE *infile = fopen(argv[1], "r");
    FILE *random_numbers_file = fopen("random-numbers", "r");

    // make sure files opened successfully
    if (infile == NULL) {
        fprintf(stderr, "Could not open infile \"%s\"\n", argv[1]);
        return 1;
    }

    if (random_numbers_file == NULL) {
        fprintf(stderr, "Could not open random numbers file\n");
        return 1;
    }

    // initial processes variables and lists
    // uint32_t total_num_of_process;  // Read from the file -- number of process to create  // commenting out for redundancy
    _process *unsorted_processes = NULL;  // A pointer to a list of total_num_of_processes processes
    
    // initialize number of processes and list from file
    TOTAL_CREATED_PROCESSES = get_num_processes(infile);
    unsorted_processes = calloc(TOTAL_CREATED_PROCESSES, sizeof(_process));
    populate_processes_from_file(infile, unsorted_processes, TOTAL_CREATED_PROCESSES);

    // sort processes by arrival time and assign IDs
    _process *sorted_processes = calloc(TOTAL_CREATED_PROCESSES, sizeof(_process));
    sort_processes_into_list(unsorted_processes, sorted_processes);

    // allocate queue
    queue = calloc(TOTAL_CREATED_PROCESSES, sizeof(uint32_t));

    // simulate all 3 techniques
    simulate(unsorted_processes, sorted_processes, FCFS, random_numbers_file);
    simulate(unsorted_processes, sorted_processes, RR, random_numbers_file);
    simulate(unsorted_processes, sorted_processes, SJF, random_numbers_file);

    // cleanup - free dynamic memory and close files
    free(unsorted_processes);
    free(sorted_processes);
    free(queue);
    fclose(infile);
    fclose(random_numbers_file);

    return 0;
}
