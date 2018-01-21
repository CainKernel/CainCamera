//
// Created by cain on 2018/1/20.
//

#include "queue.h"
#include <stdio.h>
#include <malloc.h>

typedef struct queue_node {
    struct queue_node* prev;
    struct queue_node* next;
    void *p;
} node;

static node *phead = NULL;
static int count = 0;

static node* create_node(void *pval) {
    node *pnode = NULL;
    pnode = (node *) malloc(sizeof(node));
    if (pnode) {
        pnode->prev = pnode->next = pnode;
        pnode->p = pval;
    }
    return pnode;
}

int create_queue() {
    phead = create_node(NULL);
    if (!phead) {
        return -1;
    }
    count = 0;
    return 0;
}

int queue_empty() {
    return count == 0;
}

int queue_size() {
    return count;
}

static node* get_node(int index) {
    if (index < 0 || index >= count) {
        return NULL;
    }
    if (index <= (count / 2)) {
        int i = 0;
        node *pnode = phead->next;
        while ((i++) < index)
            pnode = pnode->next;
        return pnode;
    }
    int j = 0;
    int rindex = count - index - 1;
    node *rnode = phead->prev;
    while ((j++) < rindex) {
        rnode = rnode->prev;
    }
    return rnode;
}

static node* get_first_node() {
    return get_node(0);
}

static node* get_last_node() {
    return get_node(count - 1);
}

void* queue_get(int index) {
    node *pindex = get_node(index);
    if (!pindex) {
        return NULL;
    }
    return pindex->p;
}


void* queue_get_head() {
    return queue_get(0);
}

void* queue_get_tail() {
    return queue_get(count - 1);
}


int queue_insert(int index, void* pval) {
    if (index == 0) {
        return queue_put_head(pval);
    }
    node *pindex = get_node(index);
    if (!pindex) {
        return -1;
    }
    node *pnode = create_node(pval);
    if (!pnode) {
        return -1;
    }
    pnode->prev = pindex->prev;
    pnode->next = pindex;
    pindex->prev->next = pnode;
    pindex->prev = pnode;
    count++;
    return 0;
}

int queue_put_head(void *pval) {
    node *pnode = create_node(pval);
    if (!pnode) {
        return -1;
    }
    pnode->prev = phead;
    pnode->next = phead->next;
    phead->next->prev = pnode;
    phead->next = pnode;
    count++;
    return 0;
}

int queue_put_tail(void *pval) {
    node *pnode = create_node(pval);
    if (!pnode) {
        return -1;
    }
    pnode->next = phead;
    pnode->prev = phead->prev;
    phead->prev->next = pnode;
    phead->prev = pnode;
    count++;
    return 0;
}

int queue_delete(int index) {
    node *pindex = get_node(index);
    if (!pindex) {
        return -1;
    }
    pindex->next->prev = pindex->prev;
    pindex->prev->next = pindex->next;
    free(pindex);
    count--;
    return 0;
}

int queue_remove() {
    return queue_delete(0);
}

int queue_remove_last() {
    return queue_delete(count - 1);
}

int destroy_queue() {
    if (!phead) {
        return -1;
    }
    node *pnode = phead->next;
    node *ptmp = NULL;
    while (pnode != phead) {
        ptmp = pnode;
        pnode = pnode->next;
        free(ptmp);
    }
    free(phead);
    phead = NULL;
    count = 0;
    return 0;
}