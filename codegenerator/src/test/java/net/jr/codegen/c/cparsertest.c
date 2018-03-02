
#include "C_lexer.h"
#include "C_parser.h"


int main(int argc, char **argv) {

    C_lexer_t lexer;
    C_parser_t parser;

    C_parser_init(&parser);
    C_lexer_init_from_file(&lexer, argv[1]);

    C_parser_parse(&parser, &lexer);

    return EXIT_SUCCESS;
}