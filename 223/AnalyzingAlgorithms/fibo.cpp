#include <stdio.h>
#include <chrono>
#include <iostream>

struct fibo_result {
    double result;
    double timeTaken;
} FiboResult;

double fibo(int n) {
    if (n <= 1) return n;

    // printf("Current on n=%d\n", n);

    return fibo(n - 1) + fibo(n - 2);
}

struct fibo_result measure_fibo(int n) {
    auto start = std::chrono::high_resolution_clock::now();
    double result = fibo(n);
    auto end = std::chrono::high_resolution_clock::now();

    std::chrono::duration<double> elapsed = end - start;

    return {
        result, elapsed.count()
    };
}

int main() {
    int n = 5;

    for (int n=0; n < 50; n++) {
        struct fibo_result result = measure_fibo(n);

        std::cout << "Fibonacci of " << n << " is " << result.result << " | Took " << result.timeTaken << " seconds." << std::endl;
    }

    return 0;
}
