use TMe_POS
go
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		vanlan choy
-- Create date: 3/15/2015
-- Description:	to retrieve the registered device expiration date
-- =============================================
CREATE PROCEDURE sp_GetExpirationDate
	@Id1 nvarchar(max),
	@Id2 nvarchar(max)
AS
BEGIN
	
	SET NOCOUNT ON;

    
	SELECT top 1 New_Expiration_Date from Payment_History where Device_Profile_Id = @Id1 and Profile__Id_2=@Id2 
	order by Created_Date desc
END
GO
GRANT EXECUTE ON OBJECT::TMe_POS.dbo.sp_GetExpirationDate
    TO Tme_Pos_Read_User;
	go