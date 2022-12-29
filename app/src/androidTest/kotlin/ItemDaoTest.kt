import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.inventory.data.InventoryDatabase
import com.example.inventory.data.Item
import com.example.inventory.data.ItemDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class ItemDaoTest {

    private lateinit var itemDao: ItemDao
    private lateinit var inventoryDatabase: InventoryDatabase

    private var item1 = Item(1, "Apples", 10.0, 20)
    private var item2 = Item(2, "Bananas", 15.0, 97)

    private suspend fun addOneItemToDb() {
        itemDao.insert(item1)
    }

    private suspend fun updateFirstItem(name: String, price: Double, quantity: Int) {
        itemDao.update(item1.copy(name = name, price = price, quantity = quantity))
    }

    private suspend fun deleteItem(item: Item) {
        itemDao.delete(item)
    }

    private suspend fun addTwoItemsToDb() {
        itemDao.insert(item1)
        itemDao.insert(item2)
    }

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        inventoryDatabase = Room.inMemoryDatabaseBuilder(context, InventoryDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        itemDao = inventoryDatabase.itemDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        inventoryDatabase.close()
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsItemIntoDB() = runBlocking {
        addOneItemToDb()
        val allItems = itemDao.getAllItems().first()
        assertEquals(allItems[0], item1)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetAllItems_returnsAllItemsFromDB() = runBlocking {
        addTwoItemsToDb()
        val allItems = itemDao.getAllItems().first()
        assertEquals(allItems[0], item1)
        assertEquals(allItems[1], item2)
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsFirstItem() = runBlocking {
        addOneItemToDb()
        val item = itemDao.getItem(item1.id).first()
        assertEquals(item, item1)
    }

    @Test
    @Throws(Exception::class)
    fun daoUpdate_updatesFirstItem() = runBlocking {
        val name = "Coconut"
        val price = 5.00
        val quantity = 10
        addOneItemToDb()
        updateFirstItem(name, price, quantity)
        val item = itemDao.getItem(item1.id).first()
        assertNotEquals(item, item1)
        assertEquals(item.name, name)
        assertEquals(item.price, price, 0.0)
        assertEquals(item.quantity, quantity)
    }

    @Test
    @Throws(Exception::class)
    fun daoDelete_deletesFirstItem() = runBlocking {
        addOneItemToDb()
        var item = itemDao.getItem(item1.id).firstOrNull()
        assertNotNull(item)
        deleteItem(item1)
        item = itemDao.getItem(item1.id).firstOrNull()
        assertNull(item)
    }
}