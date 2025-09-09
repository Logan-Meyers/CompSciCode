.SUFFIXES: .cpp

all:
	@echo "Usage: make <filename>.cpp"

%: %.cpp
	g++ -std=c++11 -lstdc++ -o a.out $< && clear && ./a.out

clean:
	rm -f *.out
