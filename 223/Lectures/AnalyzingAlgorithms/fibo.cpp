#include <stdio.h>
#include <chrono>
#include <iostream>

struct fibo_result {
    double result;
    double timeTaken;
} FiboResult;

static int steps = 0;

double fibo(int n) {
    if (n <= 1) return n;

    steps++;

    // printf("Current on n=%d\n", n);

    return fibo(n - 1) + fibo(n - 2);
}

struct fibo_result measure_fibo(int n) {
    auto start = std::chrono::high_resolution_clock::now();
    steps = 0;
    double result = fibo(n);
    auto end = std::chrono::high_resolution_clock::now();

    std::chrono::duration<double> elapsed = end - start;

    return {
        result, elapsed.count()
    };
}

int main() {
    for (int n=0; n < 45; n++) {
        struct fibo_result result = measure_fibo(n);

        std::cout << "Fibonacci of " << n << " is " << result.result << " | Took " << result.timeTaken << " seconds | And " << steps << " steps." << std::endl;
    }

    return 0;
}
