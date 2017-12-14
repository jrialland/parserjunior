

extern void puts(char *s);

static const char *hello="\"Hello \n World\"\x0a";


int a = {0,1,2,3,4,5,6,7,8,9};


struct _sample {
	int a;
	char b;
	char *c;
};

const struct _sample s = {3,-27, "This is a test"};

int main(void) {
	puts(s.c);
	puts((&s)->c);
	return 0;
}
