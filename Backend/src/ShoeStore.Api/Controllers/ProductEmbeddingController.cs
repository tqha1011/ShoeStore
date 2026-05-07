using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.Interface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for managing product embedding generation.
///     Handles the creation of vector embeddings for products to support semantic search capabilities.
///     Requires admin authentication to access all endpoints.
/// </summary>
/// <param name="productEmbeddingService">Service responsible for generating and storing product embeddings.</param>
[Route("api/product-embedding")]
[ApiController]
[Authorize(Roles = "Admin")]
public class ProductEmbeddingController(IProductEmbeddingService productEmbeddingService) : ControllerBase
{
    /// <summary>
    ///     Generates and stores a vector embedding for a single product.
    /// </summary>
    /// <remarks>
    ///     Requires a query parameter with:
    ///     - <c>productPublicId</c>: the unique public identifier of the product
    ///     Generates a text representation of the product (including name, brand, category, and variants)
    ///     and creates a vector embedding from that text. The embedding is persisted to the database.
    ///     Only accessible by users with Admin role.
    /// </remarks>
    /// <param name="productPublicId">The public GUID identifier of the product to embed.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="201">Embedding generated and stored successfully.</response>
    /// <response code="404">Product not found with the provided ID.</response>
    /// <response code="500">Internal server error; failed to generate or store the embedding.</response>
    /// <returns>An action result indicating success (201 Created) or detailing the error.</returns>
    [ProducesResponseType(StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPost("generate-single")]
    public async Task<IActionResult> GenerateProductVector([FromQuery] Guid productPublicId, CancellationToken token)
    {
        var result = await productEmbeddingService.GenerateVectorEmbeddingByProductPublicId(productPublicId, token);

        var response = result.Match<IActionResult>(
            _ => Created(),
            errors => errors[0].Code switch
            {
                "Product.NotFound" => NotFound(new
                {
                    message = "Product not found",
                    description = errors[0].Description
                }),
                _ => StatusCode(StatusCodes.Status500InternalServerError,new
                {
                    message = "Something went wrong",
                    description = errors[0].Description
                })
            });
        return response;
    }
    
    /// <summary>
    ///     Generates and stores vector embeddings for all existing products in batches.
    /// </summary>
    /// <remarks>
    ///     <para>
    ///         <strong>⚠️ CRITICAL - FRONTEND INSTRUCTION:</strong>
    ///     </para>
    ///     <para>
    ///         This endpoint MUST be called <strong>EXACTLY ONE TIME ONLY</strong> after the initial product database setup.
    ///         <br/>
    ///         Do NOT call this endpoint multiple times or in regular workflows, as it will create duplicate embeddings.
    ///     </para>
    ///     <para>
    ///         <strong>When to Call:</strong>
    ///         <list type="bullet">
    ///             <item>During initial system setup (one-time only)</item>
    ///             <item>After all products have been imported into the database</item>
    ///             <item>From an admin-only dashboard panel</item>
    ///             <item>NEVER from regular product management or customer-facing features</item>
    ///         </list>
    ///     </para>
    ///     <para>
    ///         <strong>What It Does:</strong>
    ///         <list type="bullet">
    ///             <item>Processes ALL products in the database in batches of 10</item>
    ///             <item>Generates text representations (product name, brand, category, variants, prices, sizes)</item>
    ///             <item>Creates AI-powered vector embeddings for semantic search</item>
    ///             <item>Persists all embeddings to the database in one operation</item>
    ///         </list>
    ///     </para>
    ///     <para>
    ///         <strong>Important Notes:</strong>
    ///         <list type="bullet">
    ///             <item>This is a long-running operation (may take several minutes)</item>
    ///             <item>Only Admin users have access (authorization required)</item>
    ///             <item>Calling this multiple times will create duplicate embeddings - no deduplication occurs</item>
    ///             <item>For new products, use the single-product endpoint instead</item>
    ///         </list>
    ///     </para>
    /// </remarks>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="201">All embeddings generated and stored successfully.</response>
    /// <response code="500">Internal server error; failed during batch embedding generation or storage.</response>
    /// <returns>An action result indicating success (201 Created) or an error message.</returns>
    [ProducesResponseType(StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPost("generate-all")]
    public async Task<IActionResult> GenerateProductVectorWithExistData(CancellationToken token)
    {
        var result = await productEmbeddingService.GenerateVectorEmbeddingWithExistDataAsync(token);
        var response = result.Match<IActionResult>(
            _ => Created(),
            errors => StatusCode(StatusCodes.Status500InternalServerError,new
            {
                message = "Something went wrong",
                description = errors[0].Description
            })
        );
        return response;
    }
}
