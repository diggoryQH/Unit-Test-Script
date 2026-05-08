package com.nongsan.selenium.model;

/**
 * Data model cho 1 dong trong bao cao Excel.
 */
public class TestResult {

    private String testCaseId;      // SP_19, DM_03, ...
    private String purpose;         // Muc dich test
    private String steps;           // Cac buoc thuc hien
    private String testData;        // Du lieu test
    private String scriptMethod;    // Ten method Java
    private String expectedResult;  // Ket qua mong doi
    private String actualResult;    // PASS / FAIL / ERROR
    private String notes;           // Ghi chu / bug

    public TestResult() {}

    public TestResult(String testCaseId, String purpose, String steps,
                      String testData, String scriptMethod,
                      String expectedResult, String actualResult, String notes) {
        this.testCaseId     = testCaseId;
        this.purpose        = purpose;
        this.steps          = steps;
        this.testData       = testData;
        this.scriptMethod   = scriptMethod;
        this.expectedResult = expectedResult;
        this.actualResult   = actualResult;
        this.notes          = notes;
    }

    // ---- Getters ----
    public String getTestCaseId()      { return testCaseId; }
    public String getPurpose()         { return purpose; }
    public String getSteps()           { return steps; }
    public String getTestData()        { return testData; }
    public String getScriptMethod()    { return scriptMethod; }
    public String getExpectedResult()  { return expectedResult; }
    public String getActualResult()    { return actualResult; }
    public String getNotes()           { return notes; }

    // ---- Setters ----
    public void setActualResult(String actualResult) { this.actualResult = actualResult; }
    public void setNotes(String notes)               { this.notes = notes; }
}
