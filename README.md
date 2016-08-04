# security-acl-jpa

An implementation of a Access Control List making use of Spring Data JPA and QueryDSL.  

## Features
* A single service for adding, removing, and checking permissions
* Discrete database tables with no foreign key constraints on your existing schema
* Fast permission checks done in one database call that can benefit from Hibernate caching
* QueryDSL predicate builder that allows you to easily append a where clause equivalent to 'where user has read permission' to existing findAll Spring Data Repository methods
* A PermissionEvaluator for integration with Spring Security Expression-Based Access Control Annotations

## TODO
* Annotation that adds permissions for a domain object after it is run: @ACLAdd(object, sid(s), permission(s))
* Annotation that removes permission for a domain object after it is run: @ACLRemove(object, sid(s), permission(s))