
typedef void (*pixelfnct_t)(int x, int y, int *r, int *g, int *b);

extern int (*IntFn)(char, int);

typedef pixelfnct_t (*fnGenerator_t)(int*, char**);

fnGenerator_t generator;