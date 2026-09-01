CC = gcc
CFLAGS = -Wall
OBJS = main.o mysum.o
mytype: type.h $(OBJS)
	$(CC) $(CFLAGS) -o mytype $(OBJS)
build: mytype
run:
	$(info Running mytype..)
	./mytype 
all: build run
clean:
	$(info Removing object files..)
	rm mytype main.o mysum.o