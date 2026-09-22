/*
	Programmer: Logan Meyers
	Class: CPTS 360
	Assignment: PA 0
	Date: 09/17/2026

	File: fstree.h
	Description: Structs and method declarations for the filesystem tree; i.e. the nodes and types of files
*/

#ifndef FILESYSTEM_H
#define FILESYSTEM_H

typedef enum file_error {
    SUCCESS,
    ALREADY_EXIST,
    NOT_EXIST,
    NOT_DIRECTORY,
    NOT_FILE,
} FILE_ERROR;

// type for a node in the file tree
typedef enum file_type {
    DIR,
    FI
} FILETYPE;

// basically a double-linked tree
typedef struct node {
	char      name[64];       // node's name string
	FILETYPE  type;           // node's file type (directory vs file)
	struct node *child, *sibling, *parent;
	// other fields if needed
} NODE;

// item type for a node in a file path
typedef enum filepath_item_type {
    SLASH,
    NAME,
} FILEPATH_ITEM_TYPE;

// singly linked list for a file path
typedef struct filepath_node {
    char name[64];
    FILEPATH_ITEM_TYPE item_type;
    struct filepath_node *child, *parent;
} FILEPATH_NODE;

/* ---------- FILE NODE METHODS ---------- */

// initialize root node
//   needs pointer to root node pointer
FILE_ERROR initialize_root(NODE** root_ptr);

// create directory (for mkdir)
//   param: pointer to root node pointer
//   param: file path to create
//   returns: ALREADY_EXIST if directory already exists
//   returns: NOT_EXIST if somewhere in the path there are missing directories
FILE_ERROR create_directory(NODE** root_ptr, FILEPATH_NODE* path_start);

// remove directory (for rmdir)
//   param: pointer to root node pointer
//   param: file path to create

/* ---------- FILE PATH METHODS ---------- */


#endif