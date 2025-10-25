# Compiler
CXX = g++
CXXFLAGS = -Wall -Wextra -std=c++11

# Target Executable
TARGET = priority_queue

# Source files
SRCS = main.cpp HeapPriorityQueue.cpp ListPriorityQueue.cpp PrinterJob.cpp

# Object files
OBJS = $(SRCS:.cpp=.o)

# Rule to build the executable
$(TARGET): $(OBJS)
	$(CXX) $(CXXFLAGS) -o $(TARGET) $(OBJS)

# Rule to compile source files into object files
%.o: %.cpp
	$(CXX) $(CXXFLAGS) -c $< -o $@

# Clean rule
clean:
	rm -f $(OBJS) $(TARGET)

# we make do man, idk! haha.
# runs clean, build, then clears and runs if previous commands successful,
# all in one go
do:
	make clean && make && clear && ./$(TARGET)