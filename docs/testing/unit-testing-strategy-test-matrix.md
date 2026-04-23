# Backend Unit Testing Strategy & Test Matrix

## Overview & Strategy
- We prioritize unit tests around core business logic in the Application layer, where pricing, stock, ownership, and state-transition rules are enforced.
- We follow a TDD-friendly workflow for new/changed behaviors: write failing tests first, implement minimally, then refactor with tests as safety rails.
- Regression prevention is mandatory for checkout and invoice flows; every bug fix in these modules must include a corresponding unit test.

## Tech Stack & Tools
- `C#`
- `xUnit`
- `Moq`

## Test Matrix

| Feature/Module | Scenario (Happy/Sad Path) | Expected Result | Status |
|---|---|---|---|
| `CheckOutService.PlaceOrderAsync` | **Sad** - Variant out of stock / insufficient stock | Returns domain validation error (e.g., `Stock.NotEnough`), no invoice persistence, no commit | Covered |
| `CheckOutService.PlaceOrderAsync` | **Sad** - `DbUpdateConcurrencyException` during flash-sale stock race | Returns conflict-style error (e.g., `Checkout.Concurrency`), transaction rollback is executed, commit is not called | Covered |
| `CheckOutService.PlaceOrderAsync` | **Sad** - User not found | Returns `User.NotFound`, no invoice created, no DB save/commit | Covered |
| `CheckOutService.PlaceOrderAsync` | **Sad** - Product variant not found | Returns `Variant.NotFound`, no invoice created, no DB save/commit | Covered |
| `CheckOutService.PlaceOrderAsync` | **Happy** - Valid request places order successfully | Returns invoice DTO, stock decremented, invoice added, `SaveChanges` called, transaction committed | Covered |
| `InvoiceService.GetInvoiceDetailAsync` | **Sad** - IDOR prevention (user tries to view another user invoice) | Access denied (`User.Unauthorized`/forbidden equivalent), data not leaked | Covered |
| `InvoiceService.UpdateInvoiceStateByAdminAsync` | **Happy** - Valid admin status transition (`Pending -> Paid`) | Status updated, response DTO reflects new state, update/save called once | Covered |
| `InvoiceService.UpdateInvoiceStateByAdminAsync` | **Sad** - Invalid admin status transition rule | Returns forbidden/validation error, no update/save | Covered |
| `InvoiceService.UpdateInvoiceStateByAdminAsync` | **Sad** - Payment amount mismatch before marking as paid | Returns `Invoice.InvalidStatus` (or equivalent), no update/save | Covered |

## Local Execution Guide (Before Opening PR)

Run from repository root (`D:\ShoeStore\Backend`):

```powershell
dotnet restore .\ShoeStore.sln
dotnet build .\ShoeStore.sln -c Debug
dotnet test .\ShoeStore.sln -c Debug --no-build
```

Optional focused run for backend unit tests only:

```powershell
dotnet test .\test\ShoeStore.Tests\ShoeStore.Tests.csproj -c Debug --no-build
```
