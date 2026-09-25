package com.core2studio.mymanager.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.core2studio.mymanager.data.local.converter.Converters
import com.core2studio.mymanager.data.local.dao.CategoryDao
import com.core2studio.mymanager.data.local.dao.ClientDao
import com.core2studio.mymanager.data.local.dao.ProductDao
import com.core2studio.mymanager.data.local.dao.OrderDao
import com.core2studio.mymanager.data.local.dao.CartItemDao
import com.core2studio.mymanager.data.local.dao.DraftOrderDao
import com.core2studio.mymanager.data.local.entity.Category
import com.core2studio.mymanager.data.local.entity.Client
import com.core2studio.mymanager.data.local.entity.Product
import com.core2studio.mymanager.data.local.entity.Order
import com.core2studio.mymanager.data.local.entity.CartItem
import com.core2studio.mymanager.data.local.entity.DraftOrder

@Database(
    entities = [Category::class, Product::class, Client::class, Order::class, CartItem::class, DraftOrder::class],
    version = 10,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MyManagerDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun clientDao(): ClientDao
    abstract fun orderDao(): OrderDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun draftOrderDao(): DraftOrderDao

    suspend fun deleteAllUserData(userId: String) {
        withTransaction {
            cartItemDao().deleteAll()
            orderDao().deleteAll()
            productDao().deleteAll()
            clientDao().deleteAll()
            categoryDao().deleteAll()
            draftOrderDao().deleteAllDrafts(userId)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: MyManagerDatabase? = null

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS draft_orders")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS draft_orders (
                        id TEXT PRIMARY KEY NOT NULL,
                        userId TEXT NOT NULL,
                        clientId TEXT NOT NULL,
                        clientName TEXT NOT NULL,
                        itemsJson TEXT NOT NULL,
                        discountType TEXT NOT NULL,
                        discountValue REAL NOT NULL,
                        gstPricingMode TEXT NOT NULL,
                        gstRate INTEGER NOT NULL,
                        gstType TEXT NOT NULL,
                        hasGstin INTEGER NOT NULL,
                        paidAmount REAL NOT NULL,
                        status TEXT NOT NULL,
                        paymentMethod TEXT NOT NULL,
                        referenceNumber TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        customFieldsJson TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_draft_orders_userId ON draft_orders(userId)")

                database.execSQL("ALTER TABLE products ADD COLUMN hsnSacCode TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE products ADD COLUMN hsnSacType TEXT NOT NULL DEFAULT 'HSN'")
            }
        }

        fun getInstance(context: Context): MyManagerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MyManagerDatabase::class.java,
                    "mymanager_database"
                )
                    .addMigrations(MIGRATION_9_10)
                    // Every released build shipped DB version 9, and 9->10 above covers
                    // those devices. The fallback is a last resort for never-released
                    // dev builds only — do NOT rely on it instead of writing migrations.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
