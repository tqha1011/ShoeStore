FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS base
USER $APP_UID
WORKDIR /app
EXPOSE 8080
EXPOSE 8081

FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
ARG BUILD_CONFIGURATION=Release
WORKDIR /

# Chú ý: Sửa thành .API (viết hoa) cho đúng tên thư mục của sếp
COPY ["src/ShoeStore.API/ShoeStore.Api.csproj", "src/ShoeStore.API/"]
COPY ["src/ShoeStore.Application/ShoeStore.Application.csproj", "src/ShoeStore.Application/"]
COPY ["src/ShoeStore.Domain/ShoeStore.Domain.csproj", "src/ShoeStore.Domain/"]
COPY ["src/ShoeStore.Infrastructure/ShoeStore.Infrastructure.csproj", "src/ShoeStore.Infrastructure/"]

# Chạy restore (Nhớ khớp đường dẫn phía trên)
RUN dotnet restore "src/ShoeStore.API/ShoeStore.Api.csproj"

COPY . .

# Chú ý: WORKDIR cũng phải là .API
WORKDIR "/src/ShoeStore.API"
RUN dotnet build "./ShoeStore.Api.csproj" -c $BUILD_CONFIGURATION -o /app/build

FROM build AS publish
ARG BUILD_CONFIGURATION=Release
RUN dotnet publish "./ShoeStore.Api.csproj" -c $BUILD_CONFIGURATION -o /app/publish /p:UseAppHost=false

FROM base AS final
WORKDIR /app
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "ShoeStore.Api.dll"]