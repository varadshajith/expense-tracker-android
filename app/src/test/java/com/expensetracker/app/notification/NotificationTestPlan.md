# Notification Flow End-to-End Testing Plan

## Overview
This document outlines the comprehensive testing plan for verifying the complete notification flow from UPI SMS detection to EditExpense screen navigation.

## Test Scenarios

### Scenario 1: Basic UPI Transaction Flow
**Objective**: Verify that a UPI SMS triggers notification and opens EditExpense screen

**Test Steps**:
1. **SMS Reception Simulation**
   - Send test SMS: "UPI: ₹150 debited from A/c **1234 to Cafe Coffee Day. UPI Ref: 123456789012"
   - Verify SmsReceiver detects UPI transaction
   - Verify SmsParser extracts amount (₹150) and merchant ("Cafe Coffee Day")

2. **Database Integration**
   - Verify expense is created in database with status "pending"
   - Verify expense ID is generated and returned
   - Verify expense contains correct amount and merchant

3. **Notification Creation**
   - Verify notification is created with correct title: "New Expense Detected"
   - Verify notification content: "₹150.00 spent at Cafe Coffee Day"
   - Verify notification has "Add Details" action button
   - Verify notification uses high priority channel

4. **Notification Click Handling**
   - Simulate notification tap
   - Verify MainActivity receives intent with action="edit_expense"
   - Verify MainActivity receives expense_id in intent extras
   - Verify ExpenseNavigation navigates to EditExpense screen
   - Verify EditExpense screen loads with correct expense data

**Expected Results**:
- ✅ SMS parsed correctly (amount: ₹150, merchant: "Cafe Coffee Day")
- ✅ Expense created in database with pending status
- ✅ Notification displayed with correct information
- ✅ Tapping notification opens EditExpense screen
- ✅ EditExpense screen shows pre-filled amount and merchant

### Scenario 2: Multiple UPI Providers
**Objective**: Test notification flow with different UPI providers

**Test Cases**:
1. **GPay Transaction**
   - SMS: "Rs.250 paid to Uber via GPay. Transaction ID: 987654321098"
   - Expected: Amount ₹250, Merchant "Uber"

2. **PhonePe Transaction**
   - SMS: "INR 1200 spent at Big Bazaar via PhonePe. UPI Ref: 112233445566"
   - Expected: Amount ₹1200, Merchant "Big Bazaar"

3. **Paytm Transaction**
   - SMS: "₹500 debited from your account to Netflix via Paytm. Transaction successful."
   - Expected: Amount ₹500, Merchant "Netflix"

4. **Bank UPI Transaction**
   - SMS: "HDFC: ₹75 paid to Local Store. UPI Ref: 556677889900"
   - Expected: Amount ₹75, Merchant "Local Store"

**Expected Results**:
- ✅ All UPI providers detected correctly
- ✅ Amount and merchant extracted accurately
- ✅ Notifications created for all transactions
- ✅ Navigation works for all notification types

### Scenario 3: Edge Cases and Error Handling
**Objective**: Test notification flow with edge cases and error conditions

**Test Cases**:
1. **Malformed SMS**
   - SMS: "Random text message"
   - Expected: No notification, no expense created

2. **Incomplete Transaction SMS**
   - SMS: "UPI: Transaction successful"
   - Expected: No notification (missing amount/merchant)

3. **Very Large Amount**
   - SMS: "UPI: ₹999999.99 debited to Merchant"
   - Expected: Notification created with large amount

4. **Special Characters in Merchant**
   - SMS: "UPI: ₹100 paid to Café & Restaurant"
   - Expected: Merchant name handled correctly

5. **Multiple Transactions in One SMS**
   - SMS: "UPI: ₹100 to Shop1 and ₹200 to Shop2"
   - Expected: Only first transaction processed

**Expected Results**:
- ✅ Malformed SMS ignored gracefully
- ✅ No crashes or exceptions
- ✅ Valid transactions processed correctly
- ✅ Error logging for debugging

### Scenario 4: Notification Permission Handling
**Objective**: Test behavior when notification permissions are denied

**Test Cases**:
1. **Notifications Enabled**
   - Verify notifications are displayed
   - Verify notification channels are created

2. **Notifications Disabled**
   - Verify app handles gracefully
   - Verify no crashes occur
   - Verify expense still created in database

3. **Partial Permissions**
   - Verify app requests necessary permissions
   - Verify graceful degradation

**Expected Results**:
- ✅ App handles permission states gracefully
- ✅ Expenses created regardless of notification permission
- ✅ No crashes when permissions denied

### Scenario 5: Background Processing
**Objective**: Test notification flow when app is in background

**Test Cases**:
1. **App in Background**
   - Send UPI SMS while app is backgrounded
   - Verify notification appears
   - Verify tapping notification brings app to foreground

2. **App Closed**
   - Send UPI SMS while app is closed
   - Verify notification appears
   - Verify tapping notification launches app

3. **Device Restart**
   - Restart device
   - Send UPI SMS
   - Verify notification flow still works

**Expected Results**:
- ✅ Notifications work in all app states
- ✅ App launches correctly from notifications
- ✅ Background processing reliable

## Manual Testing Checklist

### Pre-Test Setup
- [ ] Install app on test device
- [ ] Grant SMS permissions (READ_SMS, RECEIVE_SMS)
- [ ] Grant notification permissions
- [ ] Verify app launches successfully
- [ ] Verify database is created

### Test Execution
- [ ] Send test UPI SMS from another device
- [ ] Verify SMS is received on test device
- [ ] Verify notification appears within 5 seconds
- [ ] Verify notification content is correct
- [ ] Tap notification
- [ ] Verify app opens to EditExpense screen
- [ ] Verify expense data is pre-filled correctly
- [ ] Test "Add Details" action button
- [ ] Test "Back" navigation
- [ ] Test multiple notifications
- [ ] Test notification dismissal

### Post-Test Verification
- [ ] Verify expense exists in database
- [ ] Verify expense has correct status
- [ ] Verify no duplicate expenses created
- [ ] Verify notification channels are properly configured
- [ ] Verify no memory leaks or crashes

## Automated Test Implementation

### Unit Tests
- `NotificationFlowTest.kt`: Tests notification manager functionality
- `SmsParserTest.kt`: Tests SMS parsing logic
- `ExpenseRepositoryTest.kt`: Tests database operations

### Integration Tests
- `NotificationIntegrationTest.kt`: Tests end-to-end flow
- `SmsReceiverTest.kt`: Tests SMS receiver functionality
- `WorkManagerTest.kt`: Tests background processing

### UI Tests
- `NotificationUITest.kt`: Tests notification appearance
- `NavigationUITest.kt`: Tests screen navigation
- `EditExpenseUITest.kt`: Tests expense editing

## Performance Testing

### Response Time Requirements
- SMS detection: < 1 second
- Notification display: < 3 seconds
- App launch from notification: < 2 seconds
- Database operations: < 500ms

### Resource Usage
- Memory usage: < 50MB additional
- Battery impact: Minimal
- CPU usage: < 5% during processing

## Security Testing

### Data Privacy
- [ ] No sensitive data in notifications
- [ ] No data transmitted externally
- [ ] Proper intent security flags
- [ ] Secure PendingIntent creation

### Permission Handling
- [ ] Minimal permission requests
- [ ] Clear permission explanations
- [ ] Graceful permission denial handling

## Test Data

### Sample UPI SMS Messages
```
1. "UPI: ₹150 debited from A/c **1234 to Cafe Coffee Day. UPI Ref: 123456789012"
2. "Rs.250 paid to Uber via PhonePe. Transaction ID: 987654321098"
3. "INR 1200 spent at Big Bazaar. UPI Ref: 112233445566"
4. "₹500 debited from your account to Netflix. Transaction successful."
5. "HDFC: ₹75 paid to Local Store. UPI Ref: 556677889900"
6. "Paytm: ₹120 spent at Metro Station. Transaction ID: 334455667788"
7. "BHIM: ₹450 debited to Gas Station. UPI Ref: 778899001122"
8. "GPay: ₹200 paid to Coffee Shop. Transaction successful."
9. "ICICI: ₹800 debited from A/c **5678 to Apollo Pharmacy. UPI Ref: 990011223344"
10. "PhonePe: ₹300 transferred to Restaurant. UPI transaction completed."
```

### Expected Parsed Results
| SMS | Amount | Merchant |
|-----|--------|----------|
| 1 | ₹150.00 | Cafe Coffee Day |
| 2 | ₹250.00 | Uber |
| 3 | ₹1200.00 | Big Bazaar |
| 4 | ₹500.00 | Netflix |
| 5 | ₹75.00 | Local Store |
| 6 | ₹120.00 | Metro Station |
| 7 | ₹450.00 | Gas Station |
| 8 | ₹200.00 | Coffee Shop |
| 9 | ₹800.00 | Apollo Pharmacy |
| 10 | ₹300.00 | Restaurant |

## Success Criteria

### Functional Requirements
- ✅ All UPI transactions detected correctly
- ✅ Notifications displayed for valid transactions
- ✅ Notification tap opens EditExpense screen
- ✅ Expense data pre-filled correctly
- ✅ No false positives or missed transactions

### Non-Functional Requirements
- ✅ Response time < 3 seconds
- ✅ 99% reliability
- ✅ No crashes or exceptions
- ✅ Graceful error handling
- ✅ Minimal battery impact

### User Experience Requirements
- ✅ Clear notification content
- ✅ Intuitive navigation flow
- ✅ Consistent behavior across scenarios
- ✅ Accessible design
- ✅ Smooth animations

## Test Environment Setup

### Device Requirements
- Android 7.0+ (API 24+)
- SMS capability
- Notification support
- Minimum 2GB RAM
- Stable internet connection

### Test Tools
- Android Studio
- ADB commands
- SMS simulator
- Notification testing tools
- Performance monitoring tools

### Test Data Management
- Clean database for each test
- Consistent test SMS messages
- Isolated test environment
- Proper cleanup after tests

This comprehensive testing plan ensures that the notification flow works correctly end-to-end, providing a reliable and user-friendly experience for UPI transaction tracking.
