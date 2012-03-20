/** 
 * Copyright 2012 Rafal Lewczuk <rafal.lewczuk@jitlogic.com>
 * 
 * ZORKA is free software. You can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * ZORKA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * ZORKA. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jitlogic.zorka.agent.rankproc;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;

import com.jitlogic.zorka.agent.ZorkaError;

/**
 * 
 * @author rlewczuk
 *
 */
public class RankList<K,T> implements TabularData {

	private String listName;
	private String attrName;
	private int size;
	
	private TabularType tabularType;
	
	private RankItem<K,T>[] items;
	
	
	@SuppressWarnings("unchecked")
	public RankList(String listName, String attrName, int size, TabularType tabularType) {
		this.listName = listName;
		this.attrName = attrName;
		this.size = size;
		this.tabularType = tabularType;
		this.items = new RankItem[0];
	}
	
	
	public String getListName() {
		return listName;
	}
	
	
	public String getAttrName() {
		return attrName;
	}
	
	public void updateType(TabularType tabularType) {
		this.tabularType = tabularType;
	}
	
	
	@SuppressWarnings("unchecked")
	public void refresh(RankItem<K,T>[] items) {
		int sz = items.length > size ? size : items.length;
		
		 RankItem<K,T>[] newItems = new RankItem[sz];
		 
		 for (int i = 0; i < sz; i++)
			 newItems[i] = items[i];
		
		this.items = newItems;
	}
	
	
	public TabularType getTabularType() {
		return tabularType;
	}
	
	
	@SuppressWarnings("unchecked")
	public Object[] calculateIndex(CompositeData value) {
		if (value instanceof RankItem) {
			Object[] idx = { ((RankItem<K,T>)value).getKey() };
			return idx;
		}
		
		throw new ZorkaError("Only RankItems originating from this object are accepted.");
	}

	
	public int size() {
		return items != null ? items.length : 0;
	}
	
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	
	public boolean containsKey(Object[] key) {
		if (key.length != 1)
			throw new ZorkaError("RankList accepts only single-field keys.");

		for (RankItem<K,T> item : items)
			if (item.getKey().equals(key[0]))
					return true;
			
		return false;
	}
	
	
	public boolean containsValue(CompositeData value) {
		for (RankItem<K,T> item : items)
			if (item.equals(value))
				return true;
		return false;
	}
	
	
	public CompositeData get(Object[] key) {
		if (key.length != 1)
			throw new ZorkaError("RankList accepts only single-field keys.");

		for (RankItem<K,T> item : items)
			if (item.getKey().equals(key[0]))
				return item;
		
		return null; 
	}
	
	
	public void put(CompositeData value) {
		throw new ZorkaError("Manual putting values to ranking list is not supported.");
	}
	
	
	public CompositeData remove(Object[] key) {
		throw new ZorkaError("Manual removal of values from ranking list is not supported.");
	}
	
	
	public void putAll(CompositeData[] values) {
		throw new ZorkaError("Manual putting values to ranking list is not supported.");
	}
	
	
	public void clear() {
		
	}
	
	
	public Set<?> keySet() {
		Set<Object> keys = new HashSet<Object>();
		for (RankItem<K,T> item : items)
			keys.add(item.getKey());
		return keys;
	}
	
	
	public Collection<?> values() {
		return Arrays.asList(items);
	}
	
	
	
}
