# RAG Inventory Manual Test Cases

Use these cases against a seeded store database and the real chat endpoint after product embeddings are synced.

## 1. Product Exists And Is In Stock

User message:

```text
Shop tu van giay chay bo nam size 42
```

Expected:

- Bot calls inventory retrieval.
- Bot recommends only products present in `AllowedProductNames`.
- Bot mentions only in-stock variants.
- Bot does not state exact stock quantities.

## 2. Specific Product Does Not Exist

User message:

```text
Shop co Nike Air Jordan 1 khong?
```

Expected when the product is not in store inventory:

- Bot says the shop currently has no suitable/exact match.
- Bot does not mention Air Jordan availability, colors, sizes, or prices.
- Bot does not recommend outside products unless retrieved context contains valid `CanRecommend` alternatives.

## 3. Size Or Color Not Available

User message:

```text
Mau Runner Pro con size 41 mau trang khong?
```

Expected when that size/color is not in in-stock variants:

- Bot directly says the requested size/color is not available.
- Bot suggests only alternatives marked `RecommendationEligibility: CanRecommend`.
- Bot does not recommend out-of-stock variants as buyable options.

## 4. Query Is Too Vague Or Weak Match

User message:

```text
Cho minh doi nao hot giong Yeezy
```

Expected when retrieval returns `SearchResult: NoMatch`:

- Bot does not mention Yeezy or any other outside model as available.
- Bot asks for another size, color, budget, or style.

## 5. Follow-Up Question Uses Chat History

Conversation:

```text
User: Minh can giay chay bo
Assistant: ... Runner Pro ...
User: Con size 42 khong?
```

Expected:

- Retrieval keyword includes recent user/assistant context and the current follow-up.
- Bot answers based on the current inventory context, not only the short follow-up text.
