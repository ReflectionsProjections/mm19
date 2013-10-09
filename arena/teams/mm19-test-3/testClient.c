/*
** listener.c -- a datagram sockets "server" demo
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

#define MAXDATASIZE 8192 //Probably not a useful number for our purposes...
#define HOST "localhost"
#define PORT "6969"

typedef struct{
	char type;
	int x;
	int y;
	int health;
	int ID;
	char orient;
} ship;

typedef struct{
	int x;
	int y;
	int success;
} hit;

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

//TODO: Use strlen to determine size of strings...

char * makeShip(char type, int x, int y, char orient){
	char * buf = malloc(70);
	char * temp=buf;
	if (type=='M'){
		sprintf(temp, "\"mainShip\" : { "); //print to string
		temp=temp+15; //Ew, yucky constants!
	} else {
		sprintf(temp, "{ \"type\" : \"%c\", ", type);
		temp=temp+16;
	}
	sprintf(temp, "\"xCoord\" : %d, \"yCoord\" : %d, \"orientation\" : \"%c\" }", x, y, orient);
	
	return buf;
}

char * init(char * name){
	char * buf= malloc(MAXDATASIZE);
	char * ship1 = makeShip('M', 5, 0, 'H'); //ship1 must always be main ship!!
	char * ship2 = makeShip('P', 6, 6, 'H');
	char * ship3 = makeShip('P', 8, 8, 'H');
	char * ship4 = makeShip('P', 10, 10, 'H');
	char * ship5 = makeShip('P', 12, 12, 'H');
	char * ship6 = makeShip('P', 14, 14, 'H');
	char * ship7 = makeShip('P', 16, 16, 'H');
	char * ship8 = makeShip('P', 18, 17, 'H');
	char * ship9 = makeShip('P', 20, 18, 'H');
	char * ship10 = makeShip('P', 22, 20, 'H');
	char * ship11 = makeShip('P', 24, 30, 'H');
	char * ship12 = makeShip('D', 26, 34, 'H');
	char * ship13 = makeShip('D', 28, 56, 'V');
	char * ship14 = makeShip('D', 30, 78, 'H');
	char * ship15 = makeShip('D', 32, 4, 'H');
	char * ship16 = makeShip('D', 34, 34, 'H');
	char * ship17 = makeShip('D', 36, 36, 'H');
	char * ship18 = makeShip('D', 38, 38, 'H');
	char * ship19 = makeShip('D', 40, 40, 'H');
	//sprintf(buf, "{ \"playerName\" : \"Roger\", \"mainShip\" : { \"xCoord\" : 5, \"yCoord\" : 5, \"orientation\" : \"H\" } , \"ships\" : [ 	{  \"type\" : \"D\", \"xCoord\" : 10,\"yCoord\" : 10, \"orientation\" : \"H\" },{  \"type\" : \"P\", \"xCoord\" : 20, \"yCoord\" : 20, \"orientation\" : \"V\" }, {  \"type\" : \"D\", \"xCoord\" : 30, \"yCoord\" : 30, \"orientation\" : \"V\" }, {  \"type\" : \"P\", \"xCoord\" : 40, \"yCoord\" : 40, \"orientation\" : \"H\" } ] }\n");
	sprintf(buf, "{ \"playerName\" : \"%s\", %s, \"ships\" : [ %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s ] }\n", name, ship1, ship2, ship3, ship4, ship5, ship6, ship7, ship8, ship9, ship10, ship11, ship12, ship13, ship14, ship15, ship16, ship17, ship18, ship19);
	free(ship1);
	free(ship2);
	free(ship3);
	free(ship4);
	free(ship5);
	free(ship6);
	free(ship7);
	free(ship8);
	free(ship9);
	free(ship10);
	free(ship11);
	free(ship12);
	free(ship13);
	free(ship14);
	free(ship15);
	free(ship16);
	free(ship17);
	free(ship18);
	free(ship19);
	return buf;
}

//Returns 0 to continue, 1 to quit, 2 to send
int getTurn(char * buf,  char * token, int response, int resources, ship * ships, char ** errors, hit * hits){
	if(response==-1 || response==9001) {
		freeAll(ships, errors, hits);
		return 1;
	}
	if(response!=100) {
		freeAll(ships, errors, hits);
		return 0;
	}
	sprintf(buf, "{ \"playerToken\" : \"qwerty123\", \"shipActions\" : [ { \"ID\" : 0, \"actionID\" : \"F\", \"actionX\" : 5, \"actionY\" : 0, \"actionExtra\" : 0 } ] }\n");
	freeAll(ships, errors, hits);
	return 2;
}

int freeAll(ship * ships, char ** errors, hit * hits){
	free(ships);
	free(hits);
	int i=0;
	for(i=0; errors[i]!=0; i++){
		free(errors[i]);
	}
	free(errors);
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
		printf("health=%d\n", ships[i].health);
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
	return 0;
}

int main(void)
{
	char * buf= init("Roger"); //get starting message
	printf("%s\n", buf);
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
	printf("client: connecting to %s\n", s);

	freeaddrinfo(servinfo); // all done with this structure

	send(sockfd, buf, strlen(buf)+1, 0);

	printf("sent data\n");

	char str[MAXDATASIZE];
	if ((numbytes = recv(sockfd, str, MAXDATASIZE-1, 0)) == -1) {
	    perror("recv");
	    exit(1);
	}

	str[numbytes] = '\0';

	printf("client: received '%s', %d bytes\n",str, numbytes);

	//Determine my player token
	char * temptok = strstr(strstr(strstr(str, "playerToken"), ":"), "\"")+1;
	char * tempend = strstr(temptok, "\"");
	tempend[0]=0;
	char * token=malloc(tempend-temptok+1);
	strcpy(token, temptok);
	tempend[0]='\"';
	int res;

	while(1){
		res=getTurn(buf, token, responseCode(str), resources(str), ships(str), errors(str), hits(str));
		if(res==1) break;
		if(res==2) {
			send(sockfd, buf, strlen(buf)+1, 0);
			printf("sent data\n");
		}

		if ((numbytes = recv(sockfd, str, MAXDATASIZE-1, 0)) == -1) {
		    perror("recv");
		    exit(1);
		}

		str[numbytes] = '\0';

		printf("client: received '%s', %d bytes\n",str, numbytes);
	}

	close(sockfd);
	free(buf);
	free(token);

	return 0;
}
