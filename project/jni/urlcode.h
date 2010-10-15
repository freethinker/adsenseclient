#ifndef _URLCODE_H
#define _URLCODE_H

char from_hex(char ch);
char to_hex(char code);
char *url_encode(char *str);
char *url_decode(char *str);
#endif /* _URLCODE_H */
