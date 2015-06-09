#Sparkle CMS Plugin Management
The Content Management System designed for Micro services

# Sparkle philosophy
Sparkle CMS was designed and implemented with Cloud in mind. Sparkle architecture is thinked to be used with [Docker containers](http://en.wikipedia.org/wiki/Docker_(software)) and [Amazon AMIs](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/AMIs.html)

# Features
Sparkle CMS is implemented with following feature in mind:
 * Multiple site management
 * Site workflow management (1)
 * Multiple authors management
 * WYSWYG content authoring
 * Content workflow management (2)
 * Integrated pluggable Asset management system
 * Integrated pluggable Full text search functionalities
 * Comment management (3)

# Modules

## Registration

### Web service

### Web User Interface

## Authoring

### Web service

### Web User Interface


# License

# Hacking
Here follows instruction on how to hack project

## Project repositories
Sparkle CMS is mainly constituted by code contained in two repositories:
 - https://github.com/thebaz73/sparkle-cms (this repository)
 - https://github.com/thebaz73/cms-plugin-mgmt (plugin repository)
  
There is a third repository which is important https://github.com/thebaz73/sparkle-fe that represents a sample Blog front end interface


## Technologies selection
Sparkle CMS is a content management system built on best of breed Java technologies.

Here follows some of main technolgies used:

| Technology     | Detail            |
|----------------|-------------------|
| Core Libraries | Spring Boot 1.2.4 |
|                | Spring Framework |
|                | Spring Data |
|                | Spring Rest |
|                | Spring Security |
|Plugin Libraries| Solr |
|                | MongoBD Full text search |
|                | Fedora common JCR |
|                | JackRabbit JCR |
