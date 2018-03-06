
extern void *malloc(int size);

typedef void (*pixelfnct_t)(int x, int y, int *r, int *g, int *b);

typedef struct _image {
    int w;
    int h;
    int **red;
    int **green;
    int **blue;
} image_t, *ptr_image_t;


void foreachpixel(ptr_image_t image, pixelfnct_t fnct) {
    int offset;
    for(int y=0; y < image->h; y++) {
        int line = image->w * y;
        for(int x=0; x < image->w; x++) {
            int offset = line + x;
            fnct(x, y, image->red[offset], image->green[offset], image->blue[offset]);
        }
    }
}

void setblack(int x, int y, int *r, int *g, int *b) {
    *r = 0;
    *g = 0;
    *b = 0;
}

void paintblack(ptr_image_t image) {
    foreachpixel(image, &setblack);
}

int main(void) {
    image_t img;
    img.w = 800;
    img.h = 600;
    img.red = malloc(img.w * img.h * sizeof(int));
    img.green = malloc(img.w * img.h * sizeof(int));
    img.blue = malloc(img.w * img.h * sizeof(int));
    paintblack(&img);
    return 0;
}
