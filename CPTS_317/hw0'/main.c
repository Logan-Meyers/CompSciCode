#include <stdio.h>

// This function collects characters from the user until the array is full,
// or the user presses enter to stop collecting.
// NOTE: press space and enter for an empty word (due to mac & linux terminal restrictions)
void get_rgy_word(char* string_ptr, size_t size) {
    printf("Enter RGY word: ");
    int c, i = 0;
    while ((c = getchar()) != '\n' && c != EOF && i < size - 1) {
        if ((char)c != ' ')
            string_ptr[i++] = (char)c;
    }
    string_ptr[i] = '\0';
}

// This function checks a given word according to traffic light standards.
// "^" (empty string) is valid
// "R"/"r" is valid
// --> "G"/"g" is valid
//     --> "Y"/"y" is valid
// and any repetition of those, possibly truncated at end, is allowed
int check_word(char* string_ptr, size_t size) {
    char *cur = string_ptr;

    // 2 Boolean variables for sequence
    // - 00: red next
    // - 01: green next
    // - 10: yellow next
    int b1 = 0;
    int b2 = 0;

    // Loop through string until null terminator
    while (*cur != '\0' && *cur != '\n') {
        // if red next
        if (!b1 && !b2) {
            // if it's not Rr
            if (*cur == 'R' || *cur == 'r') {
                b1 = 0;
                b2 = 1;
            } else {
                return 0;  // 0 for fail/invalid
            }
        }

        // if green next
        else if (!b1 && b2) {
            // if it's not Gg
            if (*cur == 'G' || *cur == 'g') {
                b1 = 1;
                b2 = 0;
            } else {
                return 0;  // 0 for fail/invalid
            }
        }

        // if yellow next
        else if (b1 && !b2) {
            // if it's not Yy
            if (*cur == 'Y' || *cur == 'y') {
                b1 = 0;
                b2 = 0;
            } else {
                return 0;  // 0 for fail/invalid
            }
        }

        // increment current char ptr to move along array
        ++cur;
    }

    return 1;  // 1 for success
}

int main() {
    // Get RGY word
    const size_t WORD_SPACE = 256;
    char rgy[WORD_SPACE];
    get_rgy_word(rgy, WORD_SPACE);

    // Check word and get result
    int is_valid = check_word(rgy, WORD_SPACE);

    // Output result to user
    if (is_valid) {
        printf("Yes\n");
    } else {
        printf("No\n");
    }

    // exit nicely :)
    return 0;
}