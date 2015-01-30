#ifndef _M_CACHE_H
#define _M_CACHE_H

#include <vector>
#include <string>
#include <unordered_map>
#include <list>


template <typename K, typename V>
class Cache {
public:
    typedef typename std::vector<struct obj>::size_type index_t;
    typedef typename std::unordered_map<K, index_t> access_table_t;
    typedef typename std::list<index_t> list_t;
    typedef typename access_table_t::iterator table_pos_t;
    typedef typename list_t::iterator list_pos_t;

    using obj_size_t = std::string::size_type;
    struct obj {
	const index_t idx;
	V *data;
	table_pos_t _table_pos;
	list_pos_t  _list_pos;
	int hits;
    };

    using obj_t = typename Cache<K, V>::obj;

    class CacheDataOps {
    public:
	using getDataSizePtr = std::string::size_type (*)(V *);
	using freeDataPtr = void (*)(V *);
	using toStringDataPtr = std::string (*)(V *);
	using toStringKeyPtr = std::string (*)(K);
	
	getDataSizePtr getDataSize;
	freeDataPtr freeData;
	toStringKeyPtr toStringKey;
	toStringDataPtr toStringData;
    };

    CacheDataOps ops;

    index_t getObjNr() const { return cache_pool.size(); }
    obj_size_t getSize() const { return size; }
    bool isFull() const { return size >= limit; }

    void updateSize();

    void putData(K k, V *v);
    V *getData(K k);

    Cache(obj_size_t l, CacheDataOps mOps) : limit(l), ops(mOps) {}
    virtual ~Cache();

    std::string to_string();

private:
    obj_t& getObj(K k);
    void invalidateObj(obj_t &);

    bool incrCapacity();
    
    const index_t incrPoolNr = 10;
    const obj_size_t limit = 100;
    obj_size_t size = 0;
    static obj_size_t defaultGetObjSize(V *data);

    std::vector<obj_t> cache_pool;
    access_table_t access_table;
    list_t empt_lt;
    list_t used_lt;
};


template<typename T>
static std::string::size_type getBaseElementSize(T *data)
{
    return (data == nullptr) ? 0 : sizeof(T);
}

template<typename T>
static std::string toStringBaseElementPtr(T *data)
{
    return std::to_string(*data);
}

template<typename T>
static std::string toStringBaseElement(T data)
{
    return std::to_string(data);
}

static std::string toStringString(std::string s)
{
    return s;
}

template<typename T>
static void freeBaseElment(T *data)
{
    return;
}


#include <utility>

template<typename K, typename V>
std::string Cache<K,V>::to_string()
{
    std::string str = "used list:";
    int nr = 0;
    for(auto used : used_lt)
	str = str + "[" +  
	    ops.toStringKey(cache_pool[used]._table_pos->first) + "," +
	    ops.toStringData(cache_pool[used].data) + 
	    "[" + std::to_string(cache_pool[used].hits) + "]]\n";

    str = str + "cache slot: " + std::to_string(getObjNr()) + "\n";
    str = str + "empty list: " + std::to_string(empt_lt.size()) + "\n";
    str = str + "capacity: " + std::to_string(limit) + " bytes\n" +
	"size: " + std::to_string(size) + " bytes\n";
    return str;
}

template<typename K, typename V>
void Cache<K,V>::invalidateObj(obj_t &obj)
{
    if(obj._table_pos == access_table.end())
	return;

    obj.hits = 0;
    size -= ops.getDataSize(obj.data);

    ops.freeData(obj.data);

    access_table.erase(obj._table_pos);
    used_lt.erase(obj._list_pos);

    obj._table_pos = access_table.end();
    
    empt_lt.push_back(obj.idx);
    obj._list_pos = --empt_lt.end();
}


template<typename K, typename V>
typename Cache<K,V>::obj_t& Cache<K,V>::getObj(K k)
{
    auto pos = access_table.find(k);
    if(pos == access_table.end())
	throw std::domain_error("no such cached obj");

    obj_t &obj = cache_pool[(*pos).second];
    used_lt.erase(obj._list_pos);
    used_lt.push_back((*pos).second);
    return obj;
}

template<typename K, typename V>
inline V *Cache<K,V>::getData(K k)
{
    try{
	obj_t& res = getObj(k);
	res.hits++;
	return res.data;
    }catch(std::domain_error err){
	return nullptr;
    }
}

template<typename K, typename V>
void Cache<K,V>::putData(K k, V *v)
{
    try{
        auto obj = getObj(k);
    }catch(std::domain_error err){
	if(empt_lt.empty() && !incrCapacity()){
	    if(used_lt.empty())
		return; // both used_list/empty_list are empty

	    invalidateObj(cache_pool[used_lt.front()]);
	}

	obj_t &obj = cache_pool[empt_lt.front()];
	empt_lt.pop_front();

	used_lt.push_back(obj.idx);
	auto res = access_table.insert(std::pair<K, index_t>(k, obj.idx));
	assert(res.second == true);

	// update the object
	obj.data = v;
	obj._list_pos = --used_lt.end();
	obj._table_pos = res.first;

	size += ops.getDataSize(obj.data);
    }
}
	

template<typename K, typename V>
bool Cache<K, V>::incrCapacity()
{
    if (isFull())
	return false;
	
    index_t start = cache_pool.end() - cache_pool.begin();	
    for (int i = 0; i < incrPoolNr; i++){
	empt_lt.emplace_back(int(start + i));
	cache_pool.push_back({start + i, nullptr, access_table.end(), --empt_lt.end(), 0});
    }

    return true;
}

template<typename K, typename V>
inline typename Cache<K, V>::obj_size_t Cache<K, V>::defaultGetObjSize(V *data)
{
    return ((data == nullptr) ? 0 : sizeof(V));
}

template<typename K, typename V>
inline void Cache<K, V>::updateSize()
{
    size = 0;
    for (auto obj : cache_pool)
	size += ops.getDataSize(obj.data);
}

template<typename K, typename V>
Cache<K, V>::~Cache()
{
    for (auto obj : cache_pool)
	ops.freeData(obj.data);
}


#endif
