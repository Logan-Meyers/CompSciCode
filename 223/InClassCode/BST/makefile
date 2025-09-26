SOURCE = $(wildcard *.cpp)

clean:
	rm -f main

build: clean
	clear && g++ -std=c++11 -lstdc++ -Wall -o main $(SOURCE)

run: build
	clear && ./main

all: run