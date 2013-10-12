/*
** testClient for mm19!! In C!
**
** The purpose of this class is to do the communications with the servwer, and parse the json into a form that we can use.
*/

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include "clientAI.c"


#define HOST "localhost"
#define PORT "6969"

//code for loading a file
char *loadFile(char *filename)
{
	FILE* input = fopen(filename, "rb");
  if(input == NULL) return NULL;
 
  if(fseek(input, 0, SEEK_END) == -1) return NULL;
  long size = ftell(input);
  if(size == -1) return NULL;
  if(fseek(input, 0, SEEK_SET) == -1) return NULL;
 
  char *content =malloc( (size_t) size +1  ); 
  if(content == NULL) return NULL;
 
  fread(content, 1, (size_t)size, input);
  if(ferror(input)) {
    free(content);
    return NULL;
  }
 
  fclose(input);
  content[size] = '\0';
  return content;
}

//Credit to Beej's Guide
int sendall(int s, char *buf, int *len)
{
    int total = 0;        // how many bytes we've sent
    int bytesleft = *len; // how many we have left to send
    int n;

    while(total < *len) {
        n = send(s, buf+total, bytesleft, 0);
        if (n == -1) { break; }
        total += n;
        bytesleft -= n;
    }

    *len = total; // return number actually sent here

    return n==-1?-1:0; // return -1 on failure, 0 on success
}

// get sockaddr, IPv4 or IPv6:
void *get_in_addr(struct sockaddr *sa)
{
	if (sa->sa_family == AF_INET) {
		return &(((struct sockaddr_in*)sa)->sin_addr);
	}

	return &(((struct sockaddr_in6*)sa)->sin6_addr);
}


int responseCode(char * str){
	char * tok = strstr(strstr(str, "responseCode"), ":")+1;
	int response=atoi(tok);
	return response;
}

int resources(char * str){
	char * tok = strstr(strstr(str, "resources"), ":")+1;
	int resources=atoi(tok);
	return resources;
}

ship * ships(char * str){
	char * temp=strstr(strstr(str, "ships"), "[");
	char * tempend=strstr(temp, "]");
	temp=strstr(temp, "{");
	int i=0;
	while(tempend>temp && temp>str){
		temp=strstr(temp+1, "{");
		i++;
	}

	ship * ships=malloc(sizeof(ship)*(i+1));
	temp=strstr(strstr(strstr(str, "ships"), "[")+1, "{")+1;
	int end=i;
	for(i=0; i<end; i++){
		ships[i].orient=strstr(strstr(strstr(temp, "orientation"), ":"), "\"")[1];
		ships[i].x=atoi(strstr(strstr(temp, "xCoord"), ":")+1);
		ships[i].ID=atoi(strstr(strstr(temp, "ID"), ":")+1);
		ships[i].y=atoi(strstr(strstr(temp, "yCoord"), ":")+1);
		ships[i].type=strstr(strstr(strstr(temp, "type"), ":"), "\"")[1];
		ships[i].health=atoi(strstr(strstr(temp, "health"), ":")+1);
		temp=strstr(temp, "{")+1;
	}
	ships[end].ID=-1;

	return ships;
}

char ** errors(char * str){
	char * temp=strstr(strstr(str, "error"), "[");
	char * tempend=strstr(temp, "]");
	temp=strstr(temp, "\"")+1;
	int i=0;
	while(tempend>temp && temp>str){
		char * strend=strstr(temp, "\"");
		temp=strstr(strend+1, "\"")+1;
		i++;
	}

	char ** errors=malloc(sizeof(char *)*(i+1));
	temp=strstr(strstr(strstr(str, "error"), "["), "\"")+1;
	int end=i;
	for(i=0; i<end; i++){
		char * strend=strstr(temp, "\"");
		strend[0]=0;
		errors[i]=malloc(strlen(temp)+1);
		strcpy(errors[i], temp);
		strend[0]='\"';
		temp=strstr(strend+1, "\"")+1;
	}
	errors[end]=0;

	return errors;
}

hit * hits(char * str){
	char * temp=strstr(strstr(str, "hitReport"), "[");
	char * tempend=strstr(temp, "]");
	temp=strstr(temp, "{");
	int i=0;
	while(tempend>temp && temp>str){
		temp=strstr(temp+1, "{");
		i++;
	}

	hit * hits=malloc(sizeof(hit)*(i+1));
	temp=strstr(strstr(strstr(str, "hitReport"), "["), "{")+1;
	int end=i;
	for(i=0; i<end; i++){
		hits[i].x=atoi(strstr(strstr(temp, "xCoord"), ":")+1);
		hits[i].y=atoi(strstr(strstr(temp, "yCoord"), ":")+1);
		if(strstr(strstr(temp, "hit"), ":")[1] == 't') hits[i].success=1;
		else hits[i].success=0;
		temp=strstr(temp, "{")+1;
	}
	hits[end].success=-1;

	return hits;
}

ping * pings(char * str){
	char * temp=strstr(strstr(str, "pingReport"), "[");
	char * tempend=strstr(temp, "]");
	temp=strstr(temp, "{");
	int i=0;
	while(tempend>temp && temp>str){
		temp=strstr(temp+1, "{");
		i++;
	}

	ping * pings=malloc(sizeof(ping)*(i+1));
	temp=strstr(strstr(strstr(str, "pingReport"), "[")+1, "{")+1;
	int end=i;
	for(i=0; i<end; i++){
		pings[i].ID=atoi(strstr(strstr(temp, "ID"), ":")+1);
		pings[i].dist=atoi(strstr(strstr(temp, "distance"), ":")+1);
		temp=strstr(temp, "{")+1;
	}
	pings[end].ID=-1;

	return pings;
}

actionResponse * actionResponses(char * str){
	char * temp=strstr(strstr(str, "shipActionResults"), "[");
	char * tempend=strstr(temp, "]");
	temp=strstr(temp, "{");
	int i=0;
	while(tempend>temp && temp>str){
		temp=strstr(temp+1, "{");
		i++;
	}

	actionResponse * responses=malloc(sizeof(actionResponse)*(i+1));
	temp=strstr(strstr(strstr(str, "shipActionResults"), "[")+1, "{")+1;
	int end=i;
	for(i=0; i<end; i++){
		responses[i].ID=atoi(strstr(strstr(temp, "ID"), ":")+1);
		responses[i].result=strstr(strstr(strstr(temp, "result"), ":"), "\"")[1];
		temp=strstr(temp, "{")+1;
	}
	responses[end].ID=-1;

	return responses;
}

int main(void)
{
	char * name=loadFile("name.txt");
	char * buf= init(name); //get starting message
	int sockfd, numbytes;
	struct addrinfo hints, *servinfo, *p;
	int rv;
	char s[INET6_ADDRSTRLEN];

	memset(&hints, 0, sizeof hints);
	hints.ai_family = AF_UNSPEC;
	hints.ai_socktype = SOCK_STREAM;

	if ((rv = getaddrinfo(HOST, PORT, &hints, &servinfo)) != 0) {
		fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(rv));
		return 1;
	}

	// loop through all the results and connect to the first we can
	for(p = servinfo; p != NULL; p = p->ai_next) {
		if ((sockfd = socket(p->ai_family, p->ai_socktype,
				p->ai_protocol)) == -1) {
			perror("client: socket");
			continue;
		}

		if (connect(sockfd, p->ai_addr, p->ai_addrlen) == -1) {
			close(sockfd);
			perror("client: connect");
			continue;
		}

		break;
	}

	if (p == NULL) {
		fprintf(stderr, "client: failed to connect\n");
		return 2;
	}

	inet_ntop(p->ai_family, get_in_addr((struct sockaddr *)p->ai_addr),
			s, sizeof s);

	freeaddrinfo(servinfo); // all done with this structure

	send(sockfd, buf, strlen(buf)+1, 0);

	char str[MAXDATASIZE];
	if ((numbytes = recv(sockfd, str, MAXDATASIZE-1, 0)) == -1) {
	    perror("recv");
	    exit(1);
	}

	str[numbytes] = '\0';

	//Determine my player token
	char * temptok = strstr(strstr(strstr(str, "playerToken"), ":"), "\"")+1;
	char * tempend = strstr(temptok, "\"");
	tempend[0]=0;
	char * token=malloc(tempend-temptok+1);
	strcpy(token, temptok);
	tempend[0]='\"';
	int res;

	while(1){
		res=getTurn(buf, token, responseCode(str), resources(str), ships(str), errors(str), hits(str), actionResponses(str), pings(str));
		if(res==1) break;
		if(res==2) {
			send(sockfd, buf, strlen(buf)+1, 0);
		}

		if ((numbytes = recv(sockfd, str, MAXDATASIZE-1, 0)) == -1) {
		    perror("recv");
		    exit(1);
		}

		str[numbytes] = '\0';
	}

	close(sockfd);
	free(buf);
	free(token);
	free(name);

	return 0;
}
