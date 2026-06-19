package fr.sutoko.inapppurchase.application.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {

    @Query("SELECT * FROM purchases")
    fun observeAll(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE sku = :sku")
    fun observeBySku(sku: String): Flow<PurchaseEntity?>

    @Query(
        "SELECT * FROM purchases " +
                "WHERE backendRegistered = 0 " +
                "AND purchaseState = :purchasedState " +
                "ORDER BY purchaseTime ASC"
    )
    fun observeUnregisteredPurchases(purchasedState: Int): Flow<List<PurchaseEntity>>

    @Query("UPDATE purchases SET backendRegistered = 1 WHERE sku = :sku")
    suspend fun markBackendRegistered(sku: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PurchaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<PurchaseEntity>)

    @Query("SELECT COUNT(*) FROM purchases WHERE sku IN (:skus)")
    fun observePurchaseCountForSkus(skus: List<String>): Flow<Int>

    @Query("DELETE FROM purchases WHERE sku = :sku")
    suspend fun deleteBySku(sku: String)

    @Query("DELETE FROM purchases")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(entities: List<PurchaseEntity>) {
        deleteAll()
        upsertAll(entities)
    }
}
