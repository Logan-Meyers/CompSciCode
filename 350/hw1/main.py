from pyeda.inter import *
def main():
         f = expr("a & b | a & c | b & c")
         f = expr2bdd(f)
         print(list(f.satisfy_all()))
 
if __name__ == "__main__":
    main()