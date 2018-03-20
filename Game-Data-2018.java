		//put in auto periodic
    
    String gameData;
		gameData = DriverStation.getInstance().getGameSpecificMessage();
                if(gameData.length() > 0)
                {
		  if(gameData.charAt(0) == 'L')
		  {
			//Put left auto code here
		  } else {
			//Put right auto code here
		  }
                }
