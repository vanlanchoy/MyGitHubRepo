use TMe_POS
go

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
-- =============================================
-- Author:		vanlan choy
-- Create date: 3/15/2015
-- Description:	to retrieve any new announcement
-- =============================================
CREATE PROCEDURE sp_GetServerMessage 
	
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;

    -- Insert statements for procedure here
	SELECT [id],[Message] from dbo.Announcements
END
GO
GRANT EXECUTE ON OBJECT::TMe_POS.dbo.sp_GetServerMessage
    TO Tme_Pos_Read_User;
	go