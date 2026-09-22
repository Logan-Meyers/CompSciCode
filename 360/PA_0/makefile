# Makefile for PA4 - Movie and TV Show Search and Sort
# CPTS 223 Advanced Data Structures

CXX = gcc
CXXFLAGS = -Wall -Wextra -g

# Target executable name
TARGET = pa0_meyers

# Source files
SOURCES = pa0_meyers.c

# Object files
OBJECTS = $(SOURCES:.c=.o)

# Default target
all: $(TARGET)

# Link the executable
$(TARGET): $(OBJECTS)
	$(CXX) $(CXXFLAGS) -o $(TARGET) $(OBJECTS)

# Compile source files
%.o: %.cpp
	$(CXX) $(CXXFLAGS) -c $< -o $@

# Clean build artifacts
clean:
	rm -f $(TARGET) $(OBJECTS)

# Run the program
run: $(TARGET)
	./$(TARGET)

# Phony targets
.PHONY: all clean run
