package dev.langchain4j.voyageai.spring;

/**
 * DELIBERATE COMPILE ERROR (ci-reporting-verification). A module that does not compile produces no test report,
 * so the only thing the job summary can show for it is the Maven output. This checks that the "Surface build
 * errors" step picks that up.
 */
class CompilationIsBrokenOnPurposeTest {

    int thisDoesNotCompile = "a String is not an int";
}
