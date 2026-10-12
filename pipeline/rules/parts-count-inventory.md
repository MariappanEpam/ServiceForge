# Technician Parts Reservation Rules

## Purpose
Ensure technicians can reserve parts only from available inventory while preventing over-allocation and maintaining accurate stock levels.

## Rules

### 1. Part Selection
- Parts must be selected from a dropdown populated from the active inventory catalog.
- Only active and orderable parts shall be displayed.
- The dropdown shall show:
  - Part Name
  - Part Number
  - Current Available Quantity

### 2. Inventory Validation
- Inventory availability must be validated at the time of reservation.
- A technician cannot reserve parts that do not exist in inventory.
- Negative inventory is never allowed.

### 3. Reservation Quantity Validation
- Requested quantity must be greater than zero.
- Requested quantity must be compared against available inventory.

### 4. Sufficient Inventory Available
When:

Requested Quantity <= Available Quantity

Then:
- Reserve the full requested quantity.
- Reduce available inventory accordingly.
- Create a reservation record.
- Show confirmation to the technician.

### 5. Insufficient Inventory Available
When:

Requested Quantity > Available Quantity

Then:
- Reserve only the available quantity.
- Reduce available inventory to zero.
- Create a partial reservation record.
- Automatically create a replenishment/order request for the shortage quantity.
- Notify the technician that:
  - Only the available quantity has been reserved.
  - Remaining quantity has been ordered.
  - They will be notified when additional stock becomes available.

### 6. No Inventory Available
When:

Available Quantity = 0

Then:
- No reservation shall be created.
- Automatically create a replenishment/order request.
- Notify the technician that the part is currently out of stock and will be ordered.

### 7. Concurrent Reservations
- Inventory validation and reservation must occur within a single transaction.
- The system must prevent multiple technicians from reserving the same inventory quantity simultaneously.
- Available inventory shall be recalculated before finalizing the reservation.

### 8. Reservation Audit
The system shall store:
- Reservation ID
- Technician ID
- Part ID
- Requested Quantity
- Reserved Quantity
- Shortage Quantity
- Reservation Date/Time
- Status (Reserved, Partially Reserved, Awaiting Stock)

### 9. Notifications
For partial or unavailable reservations:
- Generate a notification to the technician.
- Notify the technician when ordered inventory is received and available for reservation.

## Examples

### Example 1 - Full Reservation
Available: 10
Requested: 4

Result:
- Reserved: 4
- Remaining Inventory: 6

### Example 2 - Partial Reservation
Available: 3
Requested: 8

Result:
- Reserved: 3
- Shortage: 5
- Order Request Created: 5

### Example 3 - Out of Stock
Available: 0
Requested: 2

Result:
- Reserved: 0
- Order Request Created: 2
- Technician notified
