package net.commoble.jumbofurnace;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SnapshotStack<T> extends SnapshotJournal<T>
{
	private T currentValue;
	private final UnaryOperator<T> copyFunction;
	
	private SnapshotStack(T value, UnaryOperator<T> copyFunction)
	{
		this.currentValue = value;
		this.copyFunction = copyFunction;
	}
	
	public static <T> SnapshotStack<T> of(T initialValue, UnaryOperator<T> copyFunction)
	{
		return new SnapshotStack<>(initialValue, copyFunction);
	}
	
	public T get()
	{
		return this.currentValue;
	}
	
	public void set(T value)
	{
		this.currentValue = value;
	}
	
	public <R> R apply(Function<T,R> function)
	{
		return function.apply(this.currentValue);
	}
	
	public void setAndTakeSnapshot(T value, TransactionContext context)
	{
		this.updateSnapshots(context);
		this.set(value);
	}
	
	public <R> R applyAndTakeSnapshot(Function<T,R> function, TransactionContext context)
	{
		this.updateSnapshots(context);
		R result = this.apply(function);
		return result;
	}
	
	@Override
	protected T createSnapshot()
	{
		return this.copyFunction.apply(this.currentValue);
	}

	@Override
	protected void revertToSnapshot(T snapshot)
	{
		this.currentValue = snapshot;
	}
}
