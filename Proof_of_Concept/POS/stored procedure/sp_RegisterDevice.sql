use TMe_POS
go

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		vanlan choy
-- Create date: 3/14/2015
-- Description:	insert device profile and create new entry for trial period
-- =============================================
CREATE PROCEDURE sp_RegisterDevice 
	@Mac_Address nvarchar(max),
	@Device_Id nvarchar(max),
	@Brand nvarchar(max),
	@Model nvarchar(max),
	@NewID int output,
	@NewID2 nvarchar(max) output
	
AS
BEGIN
	declare @tempID int
	declare @tempID2 nvarchar(max)
	declare @count int
	declare @id_table table
	(
		id int,
		id2 nvarchar(max)
	)
	
	--check if there is a match in the current table with the provided device info 
	select @NewID=Id,@NewID2=Id2 from DeviceProfile where Mac_Address=@Mac_Address
	and Device_Id=@Device_Id and Brand=@Brand and Model=@Model

	if @NewID is not null
	begin
		--skip the remaining procedure and return immediately
		return
	end

	
	
	DECLARE @myid uniqueidentifier
	SET @myid = NEWID()
	
	--start transaction here
	begin transaction [t1]
	
	begin try
		Insert into DeviceProfile (Mac_Address,Device_Id,Brand,Model,Id2)
			output inserted.Id, inserted.Id2 into @id_table
		values
		(@Mac_Address,@Device_Id,@Brand,@Model,CONVERT(varchar(255), @myid))
	
		select @tempID=id,@tempID2=id2 from @id_table

		--now use the newly inserted id and id2 to create new entry into payment history table
		Insert into Payment_History(Device_Profile_Id,Profile__Id_2,Payment_Id,New_Expiration_Date)
		values
		(@tempID,@tempID2,'Initial',DATEADD(dd,7,getDate()))
	
		set @NewID = @tempID
		set @NewID2 = @tempID2

		--commit 
		commit transaction [t1]
		
	end try
	begin catch
		rollback transaction [t1]
		insert into db_error_log ([error_number]
		,[error_serverity]
		,[error_state]
		,[error_procedure]
		,[error_line]
		,[error_message]
		,[note])
		output -1, CONVERT(varchar(255), inserted.Id) into @id_table
		values(ERROR_NUMBER()
		,ERROR_SEVERITY()
		,ERROR_STATE()
		,ERROR_PROCEDURE()
		,ERROR_LINE()
		,ERROR_MESSAGE()
		,'exec sp_RegisterDevice input values are ['+@Mac_Address+'], ['+
		 @Device_Id+'],['+@Brand+'],['+@Model+']')
		 select @NewID2='-1',@NewID=id2 from @id_table
		 
	end catch

END
GO
GRANT EXECUTE ON OBJECT::TMe_POS.dbo.sp_RegisterDevice
    TO Tme_Pos_Register_Device;
	go