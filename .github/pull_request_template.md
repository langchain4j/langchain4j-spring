<!--
Thank you so much for your contribution!

Please fill in all the sections below.
Please open the PR as a draft initially. Once it is reviewed and approved, we will ask you to add documentation and examples.
Please note that PRs with breaking changes or without tests will be rejected.

Please note that PRs will be reviewed based on the priority of the issues they address.
We ask for your patience. We are doing our best to review your PR as quickly as possible.
Please refrain from pinging and asking when it will be reviewed. Thank you for understanding!
-->

## Issue
<!-- Please specify the ID of the issue this PR is addressing. For example: "Closes #1234" or "Fixes #1234" -->
Closes #

## Change
<!-- Please describe the changes you made. -->


## General checklist
<!-- Please double-check the following points and mark them like this: [X] -->
- [ ] There are no breaking changes
- [ ] I have added unit and/or integration tests for my change
- [ ] The tests cover both positive and negative cases
- [ ] I have manually run all the unit and integration tests in the module I have added/changed, and they are all green
<!-- Before adding documentation and example(s) (below), please wait until the PR is reviewed and approved. -->
- [ ] I have added/updated the [documentation](https://github.com/langchain4j/langchain4j/tree/main/docs/docs)
- [ ] I have added an example in the [examples repo](https://github.com/langchain4j/langchain4j-examples) (only for "big" features)


## Checklist for adding new Spring Boot starter
<!-- Please double-check the following points and mark them like this: [X] -->
- [ ] I have added my new starter in the root `pom.xml`
- [ ] I have added a `org.springframework.boot.autoconfigure.AutoConfiguration.imports` file in the `langchain4j-{integration}-spring-boot-starter/src/main/resources/META-INF/spring/` directory
