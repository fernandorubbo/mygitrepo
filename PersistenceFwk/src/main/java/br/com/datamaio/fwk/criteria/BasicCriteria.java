package br.com.datamaio.fwk.criteria;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import br.com.datamaio.fwk.util.DateUtil;
import br.com.datamaio.fwk.util.ReflectionUtil;

public class BasicCriteria implements Serializable {

	private static final long serialVersionUID = 1L;

	protected Integer page; 
	protected Integer pageSize;
	protected Set<OrderCriteria> orderColumns = new HashSet<OrderCriteria>();
	protected Set<Join> joins = new HashSet<Join>();

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer limit) {
		this.page = limit;
	}

	public Integer getPageSize() {
		return pageSize;
	}
	
	public void setPageSize(Integer offset) {
		this.pageSize = offset;
	}

    public Set<OrderCriteria> getOrderColumns(){
    	return orderColumns;
    }

	public void setOrderColumns(Set<OrderCriteria> orderColumns)
    {
    	this.orderColumns = orderColumns;
    }
    
    public void addOrder(OrderCriteria order){
    	orderColumns.add(order);
    }
    
    public void addOrder(String propertyName) {
    	addOrder(new OrderCriteria(propertyName ));
    }
    
    public void addDescendingOrder(String propertyName){
    	addOrder(new OrderCriteria(propertyName, false));
    }

	public Set<Join> getJoins() {
		return joins;
	}

	public void setJoins(Set<Join> joins) {
		this.joins = joins;
	}
	
	public void addJoin(Join join){
		this.joins.add(join);
	}
	
	public boolean joinWith(final String alias)
	{
		return CollectionUtils.exists(this.joins, new Predicate() {			
			@Override
			public boolean evaluate(Object obj) {
				Join join = (Join)obj;
				return alias.equals(join.getAlias());
			}
		});
	}
	
	public boolean joinWith(Join join)
	{
		return this.joins.contains(join);
	}
	
	public void addInnerJoin(String associationPath){
		addInnerJoin(associationPath, null);
	}

	public void addInnerJoin(String associationPath, String alias){
		this.joins.add(new InnerJoin(associationPath, alias, false));
	}

	public void addLeftJoin(String associationPath){
		this.addLeftJoin(associationPath, null);
	}
	
	public void addLeftJoin(String associationPath, String alias){
		this.joins.add(new LeftJoin(associationPath, alias, false));
	}
	
	public void addInnerJoinFetch(String associationPath){
		addInnerJoinFetch(associationPath, null);
	}

	public void addInnerJoinFetch(String associationPath, String alias){
		this.joins.add(new InnerJoin(associationPath, alias, true));
	}

	public void addLeftJoinFetch(String associationPath){
		this.addLeftJoinFetch(associationPath, null);
	}
	
	public void addLeftJoinFetch(String associationPath, String alias){
		this.joins.add(new LeftJoin(associationPath, alias, true));
	}
	
	@Override
    public String toString()
    {
        final PropertyDescriptor[] descriptors = ReflectionUtil.getPropertyDescriptors(this.getClass());
 
        final StringBuilder buff = new StringBuilder(descriptors.length * 10);
        buff.append(this.getClass().getSimpleName())
        	.append(" = [");

        short count = 0;
        for(PropertyDescriptor descriptor : descriptors)
        {
            final String name = descriptor.getName();
            if ("class".equals(name))
            {
                continue;
            }

            final Method method = descriptor.getReadMethod();
            buff.append(count==0 ? "" : ", ").append(name).append(":");
            final Object obj = ReflectionUtil.invokeMethod(method, this);
            if (obj instanceof Calendar) {
                buff.append(DateUtil.format((Calendar) obj));
            } else {
                buff.append(obj);
            }
            
            count++;
        }

        buff.append("]");
        return buff.toString();
    }

	public void reset(){
		this.page = 0;
		this.pageSize = 0;
		this.orderColumns = new HashSet<OrderCriteria>();
		this.joins = new HashSet<Join>();

		final PropertyDescriptor[] descriptors = ReflectionUtil.getPropertyDescriptors(this.getClass());
		for(PropertyDescriptor descriptor : descriptors)
		{
            String name = descriptor.getName();
            if ("class".equals(name) || "page".equals(name) || "pageSize".equals(name) || "orderColumns".equals(name) || "joins".equals(name))
            {
                continue;
            }

            Method method = descriptor.getWriteMethod();
            // TODO: TESTAR COMO ELE IRA SE COMPORTAR COM TIPOS PRIMITIVOS 
            ReflectionUtil.invokeMethod(method, this, new Object[]{null});            
        }
	}
}