package com.example.shoestoreapp.features.admin.addproduct.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoestoreapp.core.utils.Resource
import com.example.shoestoreapp.features.admin.addproduct.data.remote.master_data.CategoryDto
import com.example.shoestoreapp.features.admin.addproduct.data.repositories.MasterDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FetchMasterDataViewModel(
    private val masterDataRepo: MasterDataRepository = MasterDataRepository()
) : ViewModel() {

    private val _categoriesList = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categoriesList: StateFlow<List<CategoryDto>> = _categoriesList.asStateFlow()

    private val _productName = MutableStateFlow("")
    val productName: StateFlow<String> = _productName.asStateFlow()

    private val _selectedCategoryName = MutableStateFlow("")
    val selectedCategoryName: StateFlow<String> = _selectedCategoryName.asStateFlow()

    init {
        loadCategories()
    }

    fun onProductNameChange(value: String) {
        _productName.value = value
    }

    fun onCategorySelected(categoryId: String, categoryName: String) {
        _selectedCategoryName.value = categoryName
    }

    private fun loadCategories() {
        viewModelScope.launch {
            masterDataRepo.getCategories().collect { result ->
                when (result) {
                    is Resource.Success -> _categoriesList.value = result.data
                    is Resource.Error -> _categoriesList.value = emptyList()
                    is Resource.Loading -> Unit
                }
            }
        }
    }
}

