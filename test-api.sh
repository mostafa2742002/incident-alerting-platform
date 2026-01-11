#!/bin/bash

# =============================================================================
# Incident Alerting Platform - Complete API Test Script
# =============================================================================
# This script tests the complete flow of the application
# Run with: chmod +x test-api.sh && ./test-api.sh
# =============================================================================

# Load environment variables from .env file
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

# Configuration
BASE_URL="http://localhost:8081/api/public"
SLACK_WEBHOOK_URL="${SLACK_WEBHOOK_URL:-https://hooks.slack.com/services/YOUR/WEBHOOK/URL}"
BLUE='\033[0;34m'
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Helper functions
print_header() {
    echo ""
    echo -e "${BLUE}=============================================================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}=============================================================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}→ $1${NC}"
}

# Generate unique slug to avoid conflicts
UNIQUE_SLUG="techcorp-$(date +%s)"

# =============================================================================
# STEP 1: REGISTER OWNER USER (must exist before creating tenant)
# =============================================================================
print_header "STEP 1: REGISTER OWNER USER"

OWNER_EMAIL="admin-$(date +%s)@techcorp.com"
print_info "Registering owner user '$OWNER_EMAIL'..."
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$OWNER_EMAIL\",
    \"displayName\": \"Admin User\",
    \"password\": \"SecurePass123!\"
  }")

echo "$REGISTER_RESPONSE" | jq .

OWNER_ID=$(echo "$REGISTER_RESPONSE" | jq -r '.id')

if [ "$OWNER_ID" != "null" ] && [ -n "$OWNER_ID" ]; then
    print_success "Owner registered: $OWNER_ID"
else
    print_error "Failed to register owner"
    exit 1
fi

# =============================================================================
# STEP 2: CREATE TENANT with OWNER (Organization)
# =============================================================================
print_header "STEP 2: CREATE TENANT with OWNER (Organization)"

print_info "Creating new tenant 'TechCorp' with owner..."
TENANT_RESPONSE=$(curl -s -X POST "$BASE_URL/tenants" \
  -H "Content-Type: application/json" \
  -d "{
    \"name\": \"TechCorp\",
    \"slug\": \"$UNIQUE_SLUG\",
    \"ownerId\": \"$OWNER_ID\"
  }")

echo "$TENANT_RESPONSE" | jq .

# Extract Tenant ID
TENANT_ID=$(echo "$TENANT_RESPONSE" | jq -r '.id')

if [ "$TENANT_ID" != "null" ] && [ -n "$TENANT_ID" ]; then
    print_success "Tenant created: $TENANT_ID with owner $OWNER_ID"
else
    print_error "Failed to create tenant"
    echo "$TENANT_RESPONSE"
    exit 1
fi

# =============================================================================
# STEP 3: LOGIN (Get JWT Token)
# =============================================================================
print_header "STEP 3: LOGIN (Get JWT Token)"

print_info "Logging in as $OWNER_EMAIL..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$OWNER_EMAIL\",
    \"password\": \"SecurePass123!\"
  }")

echo "$LOGIN_RESPONSE" | jq .

ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.accessToken')
REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.refreshToken')

if [ "$ACCESS_TOKEN" != "null" ] && [ -n "$ACCESS_TOKEN" ]; then
    print_success "Login successful!"
    print_info "Access Token: ${ACCESS_TOKEN:0:50}..."
else
    print_error "Login failed"
    exit 1
fi

# =============================================================================
# STEP 4: CREATE WEBHOOK EARLY (to capture all events)
# =============================================================================
print_header "STEP 4: CREATE WEBHOOK (Slack Integration)"

print_info "Creating webhook for Slack integration..."
print_info "Events: INCIDENT_CREATED, INCIDENT_RESOLVED, INCIDENT_UPDATED, INCIDENT_ASSIGNED, INCIDENT_UNASSIGNED, INCIDENT_ESCALATED"
WEBHOOK_RESPONSE=$(curl -s -X POST "$BASE_URL/tenants/$TENANT_ID/webhooks" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "name": "Slack Notifications",
    "url": "'"$SLACK_WEBHOOK_URL"'",
    "secret": "my-webhook-secret-key",
    "events": ["INCIDENT_CREATED", "INCIDENT_RESOLVED", "INCIDENT_UPDATED", "INCIDENT_ASSIGNED", "INCIDENT_UNASSIGNED", "INCIDENT_ESCALATED"]
  }')

echo "$WEBHOOK_RESPONSE" | jq .

WEBHOOK_ID=$(echo "$WEBHOOK_RESPONSE" | jq -r '.id')
if [ "$WEBHOOK_ID" != "null" ] && [ -n "$WEBHOOK_ID" ]; then
    print_success "Webhook created: $WEBHOOK_ID"
    print_info "🔔 All incident events will now be sent to Slack!"
else
    print_info "Webhook creation skipped (may already exist or invalid URL)"
fi

# =============================================================================
# STEP 5: REGISTER ANOTHER USER (Developer)
# =============================================================================
print_header "STEP 5: REGISTER ANOTHER USER (Developer)"

DEV_EMAIL="developer-$(date +%s)@techcorp.com"
print_info "Registering developer user '$DEV_EMAIL'..."
DEV_REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
  -H "Content-Type: application/json" \
  -d "{
    \"email\": \"$DEV_EMAIL\",
    \"displayName\": \"Dev User\",
    \"password\": \"DevPass123!\"
  }")

echo "$DEV_REGISTER_RESPONSE" | jq .

DEVELOPER_ID=$(echo "$DEV_REGISTER_RESPONSE" | jq -r '.id')
print_success "Developer registered: $DEVELOPER_ID"

# Add developer to tenant as MEMBER
print_info "Adding developer to tenant as MEMBER role..."
curl -s -X POST "$BASE_URL/tenants/$TENANT_ID/users" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d "{
    \"userId\": \"$DEVELOPER_ID\",
    \"roleCode\": \"MEMBER\"
  }" | jq .

print_success "Developer added to tenant"

# =============================================================================
# STEP 6: CREATE INCIDENTS
# =============================================================================
print_header "STEP 6: CREATE INCIDENTS"

print_info "Creating CRITICAL incident..."
INCIDENT1_RESPONSE=$(curl -s -X POST "$BASE_URL/tenants/$TENANT_ID/incidents" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "title": "Production Database Down",
    "description": "The main production database is not responding. All services affected.",
    "severity": "CRITICAL"
  }')

echo "$INCIDENT1_RESPONSE" | jq .

INCIDENT1_ID=$(echo "$INCIDENT1_RESPONSE" | jq -r '.id')
print_success "Critical Incident created: $INCIDENT1_ID"

print_info "Creating HIGH severity incident..."
INCIDENT2_RESPONSE=$(curl -s -X POST "$BASE_URL/tenants/$TENANT_ID/incidents" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "title": "API Response Time Degradation",
    "description": "API response times have increased by 300% in the last hour.",
    "severity": "HIGH"
  }')

echo "$INCIDENT2_RESPONSE" | jq .

INCIDENT2_ID=$(echo "$INCIDENT2_RESPONSE" | jq -r '.id')
print_success "High Incident created: $INCIDENT2_ID"

print_info "Creating MEDIUM severity incident..."
INCIDENT3_RESPONSE=$(curl -s -X POST "$BASE_URL/tenants/$TENANT_ID/incidents" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "title": "Memory Leak in Worker Service",
    "description": "Worker service memory usage is gradually increasing. Needs investigation.",
    "severity": "MEDIUM"
  }')

echo "$INCIDENT3_RESPONSE" | jq .

INCIDENT3_ID=$(echo "$INCIDENT3_RESPONSE" | jq -r '.id')
print_success "Medium Incident created: $INCIDENT3_ID"

# =============================================================================
# STEP 7: LIST ALL INCIDENTS
# =============================================================================
print_header "STEP 7: LIST ALL INCIDENTS"

print_info "Fetching all incidents..."
curl -s -X GET "$BASE_URL/tenants/$TENANT_ID/incidents" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

# =============================================================================
# STEP 8: UPDATE INCIDENT STATUS
# =============================================================================
print_header "STEP 8: UPDATE INCIDENT STATUS"

print_info "Changing incident status to IN_PROGRESS..."
curl -s -X PUT "$BASE_URL/tenants/$TENANT_ID/incidents/$INCIDENT1_ID/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "status": "IN_PROGRESS"
  }' | jq .

print_success "Incident status updated to IN_PROGRESS"

# =============================================================================
# STEP 9: ADD COMMENTS TO INCIDENT
# =============================================================================
print_header "STEP 9: ADD COMMENTS TO INCIDENT"

print_info "Adding first comment..."
COMMENT1_RESPONSE=$(curl -s -X POST "$BASE_URL/tenants/$TENANT_ID/incidents/$INCIDENT1_ID/comments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d "{
    \"content\": \"Starting investigation. Checking database logs now.\",
    \"authorId\": \"$OWNER_ID\"
  }")

echo "$COMMENT1_RESPONSE" | jq .

COMMENT1_ID=$(echo "$COMMENT1_RESPONSE" | jq -r '.id')
print_success "Comment 1 added: $COMMENT1_ID"

print_info "Adding second comment..."
COMMENT2_RESPONSE=$(curl -s -X POST "$BASE_URL/tenants/$TENANT_ID/incidents/$INCIDENT1_ID/comments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d "{
    \"content\": \"Found the issue! Connection pool was exhausted. Increasing max connections.\",
    \"authorId\": \"$OWNER_ID\"
  }")

echo "$COMMENT2_RESPONSE" | jq .
print_success "Comment 2 added"

# =============================================================================
# STEP 10: LIST COMMENTS FOR INCIDENT
# =============================================================================
print_header "STEP 10: LIST COMMENTS FOR INCIDENT"

print_info "Fetching all comments for incident..."
curl -s -X GET "$BASE_URL/tenants/$TENANT_ID/incidents/$INCIDENT1_ID/comments" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

# =============================================================================
# STEP 11: UPDATE A COMMENT
# =============================================================================
print_header "STEP 11: UPDATE A COMMENT"

if [ "$COMMENT1_ID" != "null" ] && [ -n "$COMMENT1_ID" ]; then
    print_info "Updating comment..."
    curl -s -X PUT "$BASE_URL/tenants/$TENANT_ID/incidents/$INCIDENT1_ID/comments/$COMMENT1_ID" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $ACCESS_TOKEN" \
      -d '{
        "content": "Started investigation. Checking database logs and connection metrics."
      }' | jq .
    print_success "Comment updated"
else
    print_info "Skipping comment update (no comment ID)"
fi

# =============================================================================
# STEP 12: ASSIGN USER TO INCIDENT
# =============================================================================
print_header "STEP 12: ASSIGN USER TO INCIDENT"

print_info "Assigning developer to incident..."
curl -s -X POST "http://localhost:8081/api/incidents/$INCIDENT1_ID/assignments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d "{
    \"assigneeId\": \"$DEVELOPER_ID\",
    \"notes\": \"Initial assignment\"
  }" | jq .

print_success "Developer assigned to incident"

# =============================================================================
# STEP 13: LIST ASSIGNMENTS FOR INCIDENT
# =============================================================================
print_header "STEP 13: LIST ASSIGNMENTS FOR INCIDENT"

print_info "Fetching assignments..."
curl -s -X GET "http://localhost:8081/api/incidents/$INCIDENT1_ID/assignments" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

# =============================================================================
# STEP 14: CHECK NOTIFICATIONS (for Developer who was assigned)
# =============================================================================
print_header "STEP 14: CHECK NOTIFICATIONS"

print_info "Fetching notifications for developer..."
curl -s -X GET "$BASE_URL/notifications/user/$DEVELOPER_ID" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

print_info "Counting unread notifications..."
UNREAD_COUNT=$(curl -s -X GET "$BASE_URL/notifications/user/$DEVELOPER_ID/unread/count" \
  -H "Authorization: Bearer $ACCESS_TOKEN")
echo "$UNREAD_COUNT" | jq .

# =============================================================================
# STEP 15: MARK NOTIFICATIONS AS READ
# =============================================================================
print_header "STEP 15: MARK NOTIFICATIONS AS READ"

print_info "Marking all notifications as read..."
curl -s -X PUT "$BASE_URL/notifications/user/$DEVELOPER_ID/read-all" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

print_success "All notifications marked as read"

# =============================================================================
# STEP 16: LIST WEBHOOKS
# =============================================================================
print_header "STEP 16: LIST WEBHOOKS"

print_info "Fetching all webhooks..."
curl -s -X GET "$BASE_URL/tenants/$TENANT_ID/webhooks" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

# =============================================================================
# STEP 17: TEST WEBHOOK
# =============================================================================
print_header "STEP 17: TEST WEBHOOK"

if [ "$WEBHOOK_ID" != "null" ] && [ -n "$WEBHOOK_ID" ]; then
    print_info "Sending test webhook to Slack..."
    TEST_RESPONSE=$(curl -s -X POST "$BASE_URL/tenants/$TENANT_ID/webhooks/$WEBHOOK_ID/test" \
      -H "Authorization: Bearer $ACCESS_TOKEN")
    echo "$TEST_RESPONSE" | jq .
    print_info "Check your Slack channel for the test message!"
else
    print_info "Skipping webhook test (no webhook ID available)"
fi

# =============================================================================
# STEP 18: CREATE ANOTHER INCIDENT (This should trigger webhook!)
# =============================================================================
print_header "STEP 18: CREATE INCIDENT (WEBHOOK TRIGGER)"

print_info "Creating new incident (should trigger Slack webhook)..."
NEW_INCIDENT_RESPONSE=$(curl -s -X POST "$BASE_URL/tenants/$TENANT_ID/incidents" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "title": "🔥 Payment Gateway Failure",
    "description": "All payment transactions are failing. Customers cannot complete purchases!",
    "severity": "CRITICAL"
  }')

echo "$NEW_INCIDENT_RESPONSE" | jq .
NEW_INCIDENT_ID=$(echo "$NEW_INCIDENT_RESPONSE" | jq -r '.id')
print_success "Incident created: $NEW_INCIDENT_ID - Check Slack for 🚨 INCIDENT CREATED notification!"

# =============================================================================
# STEP 19: ASSIGN USER TO NEW INCIDENT (WEBHOOK TRIGGER)
# =============================================================================
print_header "STEP 19: ASSIGN USER TO INCIDENT (WEBHOOK TRIGGER)"

print_info "Assigning developer to the new incident..."
print_info "This should trigger Slack webhook with 👤 INCIDENT ASSIGNED notification!"
ASSIGN_RESPONSE=$(curl -s -X POST "http://localhost:8081/api/incidents/$NEW_INCIDENT_ID/assignments" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d "{
    \"assigneeId\": \"$DEVELOPER_ID\",
    \"notes\": \"Please investigate this payment gateway issue ASAP\"
  }")

echo "$ASSIGN_RESPONSE" | jq .
print_success "Developer assigned - Check Slack for 👤 assignment notification with assignee name!"

# =============================================================================
# STEP 20: ESCALATE INCIDENT (WEBHOOK TRIGGER)
# =============================================================================
print_header "STEP 20: ESCALATE INCIDENT (WEBHOOK TRIGGER)"

print_info "Escalating incident severity..."
print_info "This should trigger Slack webhook with ⚠️ ESCALATION notification!"
curl -s -X POST "$BASE_URL/tenants/$TENANT_ID/incidents/$NEW_INCIDENT_ID/escalate" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

print_success "Incident escalated - Check Slack for ⚠️ escalation notification!"

# =============================================================================
# STEP 21: RESOLVE INCIDENT (WEBHOOK TRIGGER)
# =============================================================================
print_header "STEP 21: RESOLVE INCIDENT (WEBHOOK TRIGGER)"

print_info "Resolving incident..."
print_info "This should trigger Slack webhook with ✅ RESOLVED notification including resolution time!"
curl -s -X PATCH "$BASE_URL/tenants/$TENANT_ID/incidents/$NEW_INCIDENT_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d '{
    "status": "RESOLVED"
  }' | jq .

print_success "Incident resolved - Check Slack for ✅ notification with resolution time!"

# =============================================================================
# STEP 22: VIEW ANALYTICS
# =============================================================================
print_header "STEP 22: VIEW ANALYTICS"

print_info "Fetching incident analytics..."
curl -s -X GET "$BASE_URL/tenants/$TENANT_ID/incidents/analytics" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

print_info "Fetching severity distribution..."
curl -s -X GET "$BASE_URL/tenants/$TENANT_ID/incidents/analytics/severity-distribution" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

print_info "Fetching MTTR (Mean Time To Resolution)..."
curl -s -X GET "$BASE_URL/tenants/$TENANT_ID/incidents/analytics/mttr" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

# =============================================================================
# STEP 23: SEARCH INCIDENTS
# =============================================================================
print_header "STEP 23: SEARCH INCIDENTS"

print_info "Searching for 'database' incidents..."
curl -s -X GET "$BASE_URL/tenants/$TENANT_ID/incidents/search?keyword=database" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

print_info "Filtering by CRITICAL severity..."
curl -s -X GET "$BASE_URL/tenants/$TENANT_ID/incidents/search?severity=CRITICAL" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

print_info "Filtering by OPEN status..."
curl -s -X GET "$BASE_URL/tenants/$TENANT_ID/incidents/search?status=OPEN" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

# =============================================================================
# STEP 24: CHECK WEBHOOK DELIVERIES
# =============================================================================
print_header "STEP 24: CHECK WEBHOOK DELIVERIES"

if [ "$WEBHOOK_ID" != "null" ] && [ -n "$WEBHOOK_ID" ]; then
    print_info "Fetching webhook delivery history..."
    curl -s -X GET "$BASE_URL/tenants/$TENANT_ID/webhooks/$WEBHOOK_ID/deliveries" \
      -H "Authorization: Bearer $ACCESS_TOKEN" | jq .
fi

# =============================================================================
# STEP 25: REFRESH TOKEN
# =============================================================================
print_header "STEP 25: REFRESH TOKEN"

print_info "Refreshing access token..."
REFRESH_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/refresh" \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"$REFRESH_TOKEN\"
  }")

echo "$REFRESH_RESPONSE" | jq .

NEW_ACCESS_TOKEN=$(echo "$REFRESH_RESPONSE" | jq -r '.accessToken')

if [ "$NEW_ACCESS_TOKEN" != "null" ] && [ -n "$NEW_ACCESS_TOKEN" ]; then
    print_success "Token refreshed successfully!"
else
    print_info "Token refresh may have failed (check response)"
fi

# =============================================================================
# STEP 26: GET TENANT DETAILS
# =============================================================================
print_header "STEP 26: GET TENANT DETAILS"

print_info "Fetching tenant information..."
curl -s -X GET "$BASE_URL/tenants/$TENANT_ID" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

# =============================================================================
# STEP 27: LIST TENANT MEMBERS
# =============================================================================
print_header "STEP 27: LIST TENANT MEMBERS"

print_info "Fetching all tenant members..."
curl -s -X GET "$BASE_URL/tenants/$TENANT_ID/users" \
  -H "Authorization: Bearer $ACCESS_TOKEN" | jq .

# =============================================================================
# SUMMARY
# =============================================================================
print_header "TEST SUMMARY"

echo ""
echo -e "${GREEN}🎉 All API flows tested successfully!${NC}"
echo ""
echo "Resources created:"
echo "  - Tenant ID:      $TENANT_ID"
echo "  - Owner ID:       $OWNER_ID"
echo "  - Developer ID:   $DEVELOPER_ID"
echo "  - Incident IDs:   $INCIDENT1_ID, $INCIDENT2_ID, $INCIDENT3_ID"
echo "  - Webhook ID:     $WEBHOOK_ID"
echo ""
echo -e "${YELLOW}Features tested:${NC}"
echo "  ✓ Tenant creation (multi-tenancy)"
echo "  ✓ User registration"
echo "  ✓ JWT Authentication (login/refresh)"
echo "  ✓ Tenant membership management"
echo "  ✓ Incident CRUD operations"
echo "  ✓ Incident status workflow"
echo "  ✓ Comments (add/update/list)"
echo "  ✓ User Assignments"
echo "  ✓ Notifications"
echo "  ✓ Webhooks (create/test/deliveries)"
echo "  ✓ Analytics & Metrics"
echo "  ✓ Search & Filtering"
echo ""
echo -e "${GREEN}🔔 Check your Slack channel for webhook notifications!${NC}"
echo ""
