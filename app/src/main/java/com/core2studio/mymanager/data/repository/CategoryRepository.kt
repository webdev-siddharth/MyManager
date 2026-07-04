package com.core2studio.mymanager.data.repository

import com.core2studio.mymanager.data.local.dao.CategoryDao
import com.core2studio.mymanager.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    suspend fun getCategoryById(id: String): Category? = categoryDao.getCategoryById(id)

    suspend fun insertCategory(category: Category) = categoryDao.insert(category)

    suspend fun updateCategory(category: Category) = categoryDao.update(category)

    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)
}
