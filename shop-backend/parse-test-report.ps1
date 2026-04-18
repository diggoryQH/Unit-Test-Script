# Script to parse XML test report and create detailed text report
<<<<<<< HEAD

# === REQ-03: Quản lý giỏ hàng ===
# $xmlPath = "target/surefire-reports/TEST-com.nongsan.controller.CartApiTest.xml"
# $xmlPath = "target/surefire-reports/TEST-com.nongsan.controller.CartDetailApiTest.xml"

# === REQ-04: Đặt hàng & Thanh toán ===
# $xmlPath = "target/surefire-reports/TEST-com.nongsan.controller.CheckoutApiTest.xml"
# $xmlPath = "target/surefire-reports/TEST-com.nongsan.controller.VnPayControllerTest.xml"
# $xmlPath = "target/surefire-reports/TEST-com.nongsan.service.VNPayServiceTest.xml"

# === REQ-12: Xử lý đơn hàng ===
# $xmlPath = "target/surefire-reports/TEST-com.nongsan.controller.OrderApiTest.xml"
# $xmlPath = "target/surefire-reports/TEST-com.nongsan.controller.OrderDetailApiTest.xml"
$xmlPath = "target/surefire-reports/TEST-com.nongsan.controller.OrderReturnApiTest.xml"
=======
>>>>>>> bd2b4f1bfb67d9346ee8303811ab18ad70cd3208




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
