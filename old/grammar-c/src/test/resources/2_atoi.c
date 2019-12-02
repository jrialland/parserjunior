

int atoi(char *str) {
    int res = 0;
    for (int i=0;str[i]!='\0';i+=1) {
        res = res*10 + str[i] - '0';
    }
    return res;
}


int main(void) {
    return atoi("5");
}