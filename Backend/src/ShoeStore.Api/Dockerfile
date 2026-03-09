FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS base
USER $APP_UID
WORKDIR /app
EXPOSE 8080
EXPOSE 8081

FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /src
COPY ["src/ShoeStore.Api/ShoeStore.Api.csproj", "src/ShoeStore.Api/"]
COPY ["src/ShoeStore.Application/ShoeStore.Application.csproj", "src/ShoeStore.Application/"]
COPY ["src/ShoeStore.Domain/ShoeStore.Domain.csproj", "src/ShoeStore.Domain/"]
COPY ["src/ShoeStore.Infrastructure/ShoeStore.Infrastructure.csproj", "src/ShoeStore.Infrastructure/"]
RUN dotnet restore "src/ShoeStore.Api/ShoeStore.Api.csproj"
COPY . .
WORKDIR "/src/src/ShoeStore.Api"
RUN dotnet build "./ShoeStore.Api.csproj" -c $BUILD_CONFIGURATION -o /app/build

FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "./ShoeStore.Api.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "ShoeStore.Api.dll"]
