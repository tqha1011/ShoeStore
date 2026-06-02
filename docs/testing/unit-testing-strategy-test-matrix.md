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
| `StatisticsService.GetStatisticsSummaryAsync` | **Happy** - Valid metrics provided for current and previous months | Returns calculated growth percentages for revenue, invoice count, and average revenue | Covered |
| `StatisticsService.GetStatisticsSummaryAsync` | **Edge Case** - Previous month metrics are zero | Avoids divide-by-zero errors, returns 100% growth for all metrics | Covered |
| `StatisticsService.GetStatisticsChartAsync` | **Happy** - Type "week" returns 7 days of data | Returns correct date labels (dd/MM format), revenue per day, zero-fills missing days | Covered |
| `StatisticsService.GetStatisticsChartAsync` | **Happy** - Type "30days" returns last 30 days of data | Returns correct date labels (dd/MM format), revenue per day, zero-fills missing days | Covered |
| `StatisticsService.GetStatisticsChartAsync` | **Happy** - Type "12months" returns 12 months of data | Returns correct date labels (MM/yy format), revenue per month, zero-fills missing months | Covered |
| `StatisticsService.GetProductsHighestStatisticsAsync` | **Happy** - Product exists in both current and previous months | Returns calculated growth percentage based on revenue difference between periods | Covered |
| `StatisticsService.GetProductsHighestStatisticsAsync` | **Edge Case** - Product exists in current month but not in previous | Returns 100% growth without null-reference errors, safely handles missing previous data | Covered |
| `VoucherService.CreateVoucherAsync` | **Happy** - Valid request with users available | Creates voucher, persists to database, enqueues notification for non-admin users only | Covered |
| `VoucherService.CreateVoucherAsync` | **Sad** - Notification queue becomes unavailable | Throws exception when enqueueing notification, database transaction persists before queue fails | Covered |
| `VoucherService.DeleteVoucherByGuidAsync` | **Happy** - Valid voucher found | Soft-deletes voucher (`IsDeleted = true`), persists changes, no queue enqueue | Covered |
| `VoucherService.DeleteVoucherByGuidAsync` | **Sad** - Voucher not found | Returns `VOUCHER_NOT_FOUND` error, no update/save, database remains safe | Covered |
| `VoucherService.UpdateVoucherAsync` | **Happy** - Valid voucher found | Updates all properties (name, discount, scope, type, quantity, etc.), persists changes | Covered |
| `VoucherService.UpdateVoucherAsync` | **Sad** - Voucher not found | Returns `VOUCHER_NOT_FOUND` error, no update/save | Covered |
| `VoucherService.DeleteVoucherExpireAsync` | **Happy** - Expired and unexpired vouchers exist | Soft-deletes only expired vouchers (`ValidTo < UtcNow`), leaves unexpired vouchers intact, no queue enqueue | Covered |
| `VoucherService.DeleteVoucherExpireAsync` | **Sad** - Cancellation token is cancelled | Throws `OperationCanceledException`, no update/save operations occur | Covered |
| `VoucherService.GetVoucherForAdminAsync` | **Happy** - Vouchers exist with mixed deletion states | Returns paginated result excluding soft-deleted vouchers, sorted by `CreatedAt` descending | Covered |
| `VoucherService.GetVoucherForAdminAsync` | **Sad** - Repository throws exception | Propagates database exception without masking, allowing caller to handle errors | Covered |
| `VoucherService.NotifyUserAboutNewVoucherAsync` | **Happy** - Multiple users including admins exist | Enqueues notification only to User-role users (filters out admins), includes voucher metadata (`VoucherId`, `VoucherName`, `ValidTo`) | Covered |
| `VoucherService.NotifyUserAboutNewVoucherAsync` | **Sad** - Notification queue becomes unavailable | Throws exception when enqueueing, no retry logic applied | Covered |
| `AuthService.LoginAsync` | **Sad** - Email or password is incorrect | Returns `Invalid.Credential` error, no token generated, database remains unchanged | Covered |
| `AuthService.LoginAsync` | **Sad** - User does not exist | Returns `Invalid.Credential` error, no token generated, database remains unchanged | Covered |
| `AuthService.LoginWithSocialAsync` | **Sad** - Social auth strategy returns error | Propagates error from strategy, no user lookup/token generation, no database save | Covered |
| `AuthService.LoginWithSocialAsync` | **Happy** - User already exists with social account | Returns JWT token, user lookup occurs, token service called once | Covered |
| `AuthService.RegisterAsync` | **Sad** - Email already exists | Returns `Email.Exist` conflict error, no password hash or user creation, database remains safe | Covered |
| `AuthService.RegisterAsync` | **Happy** - Email is unique and available | Creates user with hashed password, extracts username from email, assigns User role, persists to database | Covered |
| `GoogleAuthStrategy.VerifySocialToken` | **Sad** - Google Client ID is missing or empty | Returns `Google.MissingClientId` error, no validator call | Covered |
| `GoogleAuthStrategy.VerifySocialToken` | **Sad** - Google token payload is null/empty | Returns `Google.EmptyPayload` error without processing | Covered |
| `GoogleAuthStrategy.VerifySocialToken` | **Sad** - Google token is invalid (JWT exception) | Returns `Google.InvalidToken` error, catches JWT parsing failures | Covered |
| `GoogleAuthStrategy.VerifySocialToken` | **Sad** - Server error during Google validation | Returns `Google.VerificationFailed` error, catches unexpected exceptions | Covered |
| `GoogleAuthStrategy.VerifySocialToken` | **Happy** - Google token is valid | Returns `SocialUserDto` containing email, name, and subject ID | Covered |
| `CartItemService.AddCartItemAsync` | **Sad** - Product variant not found | Returns `ProductVariant.NotFound` error, no cart item add/save | Covered |
| `CartItemService.AddCartItemAsync` | **Sad** - User is not found | Returns `User.NotFound` error, no cart item add/save | Covered |
| `CartItemService.AddCartItemAsync` | **Sad** - Request quantity exceeds variant stock (no existing item) | Returns `CartItem.QuantityExceedsStock` validation error, no database save | Covered |
| `CartItemService.AddCartItemAsync` | **Sad** - Request quantity + existing quantity exceeds stock | Returns `CartItem.QuantityExceedsStock` validation error, no quantity increment/save | Covered |
| `CartItemService.AddCartItemAsync` | **Happy** - Cart item already exists for variant | Increments quantity on existing item, returns updated item DTO, persists changes | Covered |
| `CartItemService.AddCartItemAsync` | **Happy** - Cart item does not exist for variant | Creates new cart item with requested quantity, returns created item DTO, persists changes | Covered |
| `CartItemService.DeleteCartItemAsync` | **Sad** - No cart items found for deletion | Returns `CartItem.NotFound` error, no delete/save | Covered |
| `CartItemService.DeleteCartItemAsync` | **Sad** - User is not authorized (cart item belongs to different user) | Returns `User.Unauthorized` error, no delete/save, prevents IDOR | Covered |
| `CartItemService.DeleteCartItemAsync` | **Happy** - Cart items exist and belong to user | Deletes requested items, persists changes, operation succeeds | Covered |
| `CartItemService.UpdateCartItemAsync` | **Sad** - Cart item not found | Returns `CartItem.NotFound` error, no update/save | Covered |
| `CartItemService.UpdateCartItemAsync` | **Sad** - Product variant not found | Returns `ProductVariant.NotFound` error, no quantity update/save | Covered |
| `CartItemService.UpdateCartItemAsync` | **Sad** - Updated quantity exceeds variant stock | Returns `CartItem.QuantityExceedsStock` validation error, no quantity update/save | Covered |
| `CartItemService.UpdateCartItemAsync` | **Happy** - Updated quantity is within stock limits | Updates quantity, returns updated item DTO with variant details, persists changes | Covered |
| `CartItemService.GetCartItemsByUserIdAsync` | **Sad** - User not found | Returns `User.NotFound` error, no cart items fetched | Covered |
| `CartItemService.GetCartItemsByUserIdAsync` | **Happy** - Cart items exist for user | Returns mapped list of cart items with variant metadata (color, size, price, stock, image), no database save | Covered |

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
