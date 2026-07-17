package com.core2studio.mymanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.core2studio.mymanager.data.local.converter.Converters
import com.core2studio.mymanager.data.local.dao.CategoryDao
import com.core2studio.mymanager.data.local.dao.ClientDao
import com.core2studio.mymanager.data.local.dao.ProductDao
import com.core2studio.mymanager.data.local.dao.OrderDao
import com.core2studio.mymanager.data.local.dao.CartItemDao
import com.core2studio.mymanager.data.local.entity.Category
import com.core2studio.mymanager.data.local.entity.Client
import com.core2studio.mymanager.data.local.entity.Product
import com.core2studio.mymanager.data.local.entity.Order
import com.core2studio.mymanager.data.local.entity.CartItem

@Database(
    entities = [Category::class, Product::class, Client::class, Order::class, CartItem::class],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MyManagerDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun clientDao(): ClientDao
    abstract fun orderDao(): OrderDao
    abstract fun cartItemDao(): CartItemDao

    companion object {
        @Volatile
        private var INSTANCE: MyManagerDatabase? = null

        fun getInstance(context: Context): MyManagerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MyManagerDatabase::class.java,
                    "mymanager_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
