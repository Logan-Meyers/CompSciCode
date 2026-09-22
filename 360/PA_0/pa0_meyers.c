/*
	Programmer: Logan Meyers
	Class: CPTS 360
	Assignment: PA 0
	Date: 09/17/2026

	File: pa0_base.c
	Description: Entry point into the unix filesytem tree program/simulator
*/

#include <stdio.h>
#include <stdlib.h>
// additional headers as needed
#include <string.h>

#include "./fstree.h"

// GLOBAL VARIABLES
NODE *root;
NODE *cwd;
char *cmd[] = {"mkdir", "rmdir", "cd", "ls", "pwd", "creat", "rm", "save", "reload", "quit"};  // commands user can run
// other global variables

// Initialize the 
int initialize() {
	root = (NODE *)malloc(sizeof(NODE));
	strcpy(root->name, "/");
	root->parent = root;
	root->sibling = 0;
	root->child = 0;
	root->type = 'D';
	cwd = root;
	// other steps as needed
	
	printf("Filesystem initialized!\n");

	// TODO: return
}

int main() {
	initialize();
	// other initialization as needed

	while(1) {
		// prompt user
		printf("Enter command: ");
		
		// scan user input

		// run command
		
		// split command into two parts: command and [argument]

		// switch for calling command, and pass in [argument]
	}
}



void mkdir() {
	// check an argument was given; if not, error

	// convert argument into a file path
}
void rmdir() {

}
void cd() {

}
void ls() {

}
void pwd() {

}
void creat() {

}
void rm() {

}
void save() {

}
void reload() {

}
void quit() {

}