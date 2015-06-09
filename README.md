# Sparkle CMS
The Content Management System designed for Micro services

**Table of Contents**

- [Sparkle philosophy](#sparkle-philosophy)
- [Features](#features)
- [Modules](#modules)
	- [Registration](#registration)
		- [Web service](#web-service)
		- [Web User Interface](#web-user-interface)
	- [Authoring](#authoring)
		- [Web service](#web-service)
		- [Web User Interface](#web-user-interface)
- [License](#license)
- [Hacking](#hacking)
	- [Project repositories](#project-repositories)
	- [Technologies selection](#technologies-selection)

# Sparkle philosophy
Sparkle CMS was designed and implemented with Cloud in mind. Sparkle architecture has been thought to be used with [Docker containers](https://www.docker.com/) and [Amazon AMIs](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/AMIs.html)

# Features
Sparkle CMS is implemented with following feature in mind:

 * Multiple site management
 * Site workflow management [(1)](#note-1)
 * Multiple authors management
 * WYSWYG content authoring
 * Content workflow management [(2)](#note-2)
 * Integrated pluggable Asset management system
 * Integrated pluggable Full text search functionalities
 * Comment management [(3)](#note-3)

#### Note 1: Only simple site workflow management in version 1.0-SNAPSHOT
#### Note 2: Still not implemented in version 1.0-SNAPSHOT
#### Note 3: Still under development

# Modules
Sparkle CMS is composed by different modules: each with a specific functionality.

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

 - [https://github.com/thebaz73/sparkle-cms](https://github.com/thebaz73/sparkle-cms) (this repository)
 - [https://github.com/thebaz73/cms-plugin-mgmt](https://github.com/thebaz73/cms-plugin-mgmt) (plugin repository)
  
There is a third important repository [https://github.com/thebaz73/sparkle-fe](https://github.com/thebaz73/sparkle-fe) that contains a sample Play Framework 2 & Bootstrap 3 implementation of a Blog front-end


## Technologies selection
Sparkle CMS is a content management system built on best of breed Java technologies.

Here follows some of main technologies used:

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
