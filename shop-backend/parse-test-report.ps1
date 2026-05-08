# Script to parse XML test report and create detailed text report

# $xmlPath = "target/surefire-reports/TEST-com.nongsan.controller.UserApiTest.xml"
# $xmlPath = "target/surefire-reports/TEST-com.nongsan.controller.StatisticalApiTest.xml"
# $xmlPath = "target/surefire-reports/TEST-com.nongsan.controller.ResetPasswordControllerTest.xml"
#$xmlPath = "target/surefire-reports/TEST-com.nongsan.service.SendMailServiceTest.xml"
$xmlPath = "target/surefire-reports/TEST-com.nongsan.controller.RateApiTest.xml"



$outputPath = "target/test-results-detail.txt"

if (-not (Test-Path $xmlPath)) {
    Write-Host "XML report not found at $xmlPath"
    exit 1
}

# Load XML
[xml]$xml = Get-Content $xmlPath

$output = @()
$output += "=" * 80
$output += "TEST EXECUTION DETAILED REPORT"
$output += "=" * 80
$output += ""

# Get test suite info
$testsuite = $xml.testsuite
$output += "Test Class: $($testsuite.name)"
$output += "Total Tests: $($testsuite.tests)"
$output += "Passed: $([int]$testsuite.tests - [int]$testsuite.failures - [int]$testsuite.errors)"
$output += "Failed: $($testsuite.failures)"
$output += "Errors: $($testsuite.errors)"
$output += "Skipped: $($testsuite.skipped)"
$output += "Time: $($testsuite.time) seconds"
$output += ""
$output += "=" * 80
$output += "DETAILED TEST RESULTS"
$output += "=" * 80
$output += ""

# Parse each test case
foreach ($testcase in $testsuite.testcase) {
    $testName = $testcase.name
    $className = $testcase.classname
    $time = $testcase.time
    
    if ($testcase.failure) {
        $output += "[FAILED] $testName ($($time)s)"
        $output += "  Error: $($testcase.failure.'#text')"
        $output += ""
    } elseif ($testcase.error) {
        $output += "[ERROR] $testName ($($time)s)"
        $output += "  Error: $($testcase.error.'#text')"
        $output += ""
    } else {
        $output += "[PASSED] $testName ($($time)s)"
    }
}

$output += ""
$output += "=" * 80
$output += "END OF REPORT"
$output += "=" * 80

# Write to file
$output | Out-File -FilePath $outputPath -Encoding UTF8

Write-Host "Test report saved to: $outputPath"
Get-Content $outputPath
