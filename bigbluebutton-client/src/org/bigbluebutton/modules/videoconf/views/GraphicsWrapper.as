package org.bigbluebutton.modules.videoconf.views
{
    import flash.display.DisplayObject;
    import flash.net.NetConnection;
    import mx.containers.Canvas;
    import mx.core.UIComponent;
    import mx.events.FlexEvent;
    import mx.utils.ObjectUtil;

    import org.bigbluebutton.core.UsersUtil;
    import org.bigbluebutton.core.model.VideoProfile;
    import org.bigbluebutton.main.model.users.BBBUser;
    import org.bigbluebutton.modules.videoconf.model.VideoConfOptions;
    import org.bigbluebutton.modules.videoconf.views.UserGraphicHolder;

    public class GraphicsWrapper extends Canvas {

        private var _options:VideoConfOptions = new VideoConfOptions();
        private var priorityWeight:Number = 2/3;
        public function GraphicsWrapper() {
            percentWidth = percentHeight = 100;
        }

        override public function addChild(child:DisplayObject):DisplayObject {
            throw("You should add the helper functions to add children to this Canvas: addAvatarFor, addVideoFor, addCameraFor");
            return null;
        }

        private function get minContentAspectRatio():Number {
            var result:Number = Number.MAX_VALUE;
            for (var i:int = 0; i < numChildren; ++i) {
                var item:UserGraphicHolder = getChildAt(i) as UserGraphicHolder;
                if (item.contentAspectRatio < result) {
                    result = item.contentAspectRatio;
                }
            }
            return result;
        }

        private function calculateCellDimensions(canvasWidth:int, canvasHeight:int, numColumns:int, numRows:int, cellAspectRatio:Number):Object {
            var obj:Object = {
width: Math.floor(canvasWidth / numColumns),
       height: Math.floor(canvasHeight / numRows)
            }
            if (obj.width / obj.height > cellAspectRatio) {
                obj.width = Math.floor(obj.height * cellAspectRatio);
            } else {
                obj.height = Math.floor(obj.width / cellAspectRatio);
            }
            return obj;
        }

        private function calculateOccupiedArea(canvasWidth:int, canvasHeight:int, numColumns:int, numRows:int, cellAspectRatio:Number):Object {
            var obj:Object = calculateCellDimensions(canvasWidth, canvasHeight, numColumns, numRows, cellAspectRatio);
            obj.occupiedArea = obj.width * obj.height * numChildren;
            obj.numColumns = numColumns;
            obj.numRows = numRows;
            obj.cellAspectRatio = cellAspectRatio;
            return obj;
        }

        private function findBestConfiguration(canvasWidth:int, canvasHeight:int, numChildrenInCanvas:int):Object {
            var cellAspectRatio:Number = minContentAspectRatio;

            var bestConfiguration:Object = {
occupiedArea: 0
            }

            for (var numColumns:int = 1; numColumns <= numChildrenInCanvas; ++numColumns) {
                var numRows:int = Math.ceil(numChildrenInCanvas / numColumns);

                var currentConfiguration:Object = calculateOccupiedArea(canvasWidth, canvasHeight, numColumns, numRows, cellAspectRatio);
                if (currentConfiguration.occupiedArea > bestConfiguration.occupiedArea) {
                    bestConfiguration = currentConfiguration;
                }
            }
            return bestConfiguration;
        }

        private function updateDisplayListHelper():void {
            if (numChildren == 0) {
                return;
            }

            var bestConfiguration:Object = findBestConfiguration(width,height,numChildren);
            var numColumns:int = bestConfiguration.numColumns;
            var numRows:int = bestConfiguration.numRows;
            var cellWidth:int = bestConfiguration.width;
            var cellHeight:int = bestConfiguration.height;
            var cellAspectRatio:Number = bestConfiguration.cellAspectRatio;

            var blockX:int = Math.floor((width - cellWidth * numColumns) / 2);
            var blockY:int = Math.floor((height - cellHeight * numRows) / 2);

            for (var i:int = 0; i < numChildren; ++i) {
                var item:UserGraphicHolder = getChildAt(i) as UserGraphicHolder;
                var cellOffsetX:int = 0;
                var cellOffsetY:int = 0;
                if (item.contentAspectRatio > cellAspectRatio) {
                    item.width = cellWidth;
                    item.height = Math.floor(cellWidth / item.contentAspectRatio);
                    cellOffsetY = (cellHeight - item.height) / 2;
                } else {
                    item.width = Math.floor(cellHeight * item.contentAspectRatio);
                    item.height = cellHeight;
                    cellOffsetX = (cellWidth - item.width) / 2;
                }
                item.x = (i % numColumns) * cellWidth + blockX + cellOffsetX;
                item.y = Math.floor(i / numColumns) * cellHeight + blockY + cellOffsetY;
            }
        }    

        private function findPriorityConfiguration2():Object{
            var bestConf:Object = {
                 isVerticalSplit: true,
                 priorityWidth: 0,
                 priorityHeight: 0,
                 otherWidth: 0,
                 otherHeight: 0,
                 numColumns: 0,
                 numRows: 0,
                 aspectRatio: 0                
            };
            var pBestConf:Object = findBestConfiguration(Math.floor(width* ((numChildren > 1) ? priorityWeight : 1)), height, 1);
            bestConf.priorityHeight = pBestConf.height;
            bestConf.priorityWidth = pBestConf.width;           
            if (numChildren > 1){
                var oBestConf:Object = findBestConfiguration(width - pBestConf.width, height, numChildren-1);
                bestConf.otherHeight=oBestConf.height;
                bestConf.otherWidth=oBestConf.width;
                bestConf.numColumns=oBestConf.numColumns;
                bestConf.numRows=oBestConf.numRows;
                bestConf.aspectRatio=oBestConf.cellAspectRatio;
            }
            return bestConf;
        }

        private function updateDisplayListHelperByPriority2():void {
            if (numChildren == 0) {
                return;
            }

            var bestConfiguration:Object = findPriorityConfiguration2();
            var numColumns:int = bestConfiguration.numColumns;
            var numRows:int = bestConfiguration.numRows;
            var oWidth:int = bestConfiguration.otherWidth;
            var oHeight:int = bestConfiguration.otherHeight;
            var pWidth:int = bestConfiguration.priorityWidth;
            var pHeight:int = bestConfiguration.priorityHeight;  
            var aspectRatio:Number = bestConfiguration.cellAspectRatio;

            var cellOffsetX:int = 0;
            var cellOffsetY:int = 0;

            var item:UserGraphicHolder = getChildAt(0) as UserGraphicHolder;
            item.width =pWidth;
            item.height = pHeight;
            item.x = Math.floor((width-pWidth-oWidth*numColumns)/2);
            item.y = Math.floor((height-pHeight)/2);
            var blockX:int = Math.floor((3*(width - oWidth*numColumns) + pWidth)/4);
            var blockY:int = Math.floor((height-oHeight*numRows)/2);
            for (var i:int = 1; i < numChildren; ++i) {
                item = getChildAt(i) as UserGraphicHolder; 
                item.width = oWidth;
           //     item.height = Math.floor(oWidth / item.contentAspectRatio);
                item.height = oHeight;
                
                item.x = ((i-1) % numColumns) * oWidth + blockX;
                item.y = Math.floor((i-1) / numColumns) * oHeight + blockY;
            }
        }

        private function findPriorityConfiguration():Object{
            var aspectRatio:Number = minContentAspectRatio;
            var bestConfig:Object = 
            /*
            {
                 isVerticalSplit: true,
                 priorityWidth: width,
                 priorityHeight: width / aspectRatio,
                 otherWidth: 0,
                 otherHeight: 0	
            };
            */
            ///*
            {
                 isVerticalSplit: false,
                 priorityWidth: height * aspectRatio,
                 priorityHeight: height,
                 otherWidth: 0,
                 otherHeight: 0
            }
            //*/
            if (numChildren > 1){
                bestConfig.priorityWidth=  Math.floor(priorityWeight * bestConfig.priorityWidth);
                bestConfig.priorityHeight= Math.floor(priorityWeight * bestConfig.priorityHeight);
                bestConfig.otherWidth= Math.floor((1-priorityWeight) * bestConfig.priorityWidth );
                bestConfig.otherHeight= Math.floor((1-priorityWeight) * bestConfig.priorityHeight);
                var nonPriorityRatio:Number = bestConfig.isVerticalSplit ?
                                                height / (bestConfig.otherHeight * (numChildren - 1)) :
                                                width  / (bestConfig.otherWidth  * (numChildren - 1));
                if(nonPriorityRatio < 1){
                    bestConfig.otherWidth*=nonPriorityRatio;
                    bestConfig.otherHeight*=nonPriorityRatio;
                }
            }
            var priorityRatio:Number = bestConfig.isVerticalSplit ?
                                        height / (bestConfig.priorityHeight) :
                                        width  / (bestConfig.priorityWidth);  
            if(priorityRatio < 1){
                bestConfig.priorityHeight*=priorityRatio;
                bestConfig.priorityWidth*=priorityRatio;
            }
            return bestConfig;
        }

        private function updateDisplayListHelperByPriority():void {
            if (numChildren == 0) {
                return;
            }

            var bestConfiguration:Object = findPriorityConfiguration();
            var oWidth:int = bestConfiguration.otherWidth;
            var oHeight:int = bestConfiguration.otherHeight;
            var pHeight:int = bestConfiguration.priorityHeight;
            var pWidth:int = bestConfiguration.priorityWidth;
            var isVertical:Boolean = bestConfiguration.isVerticalSplit;
            var item:UserGraphicHolder = getChildAt(0) as UserGraphicHolder;
            var oX:int;
            var oY:int;
            var relativPosY:int;
            item.width =pWidth;
            item.height = pHeight;
            if(isVertical){
                item.x = (width-pWidth-oWidth)/2;
                item.y = (height-pHeight)/2;
                oX = (width+pWidth-oWidth)/2;
                oY = (height - (oHeight * (numChildren-1)))/2;
                relativPosY = 1;
            } else {
                item.y = (height-pHeight-oHeight)/2;
                item.x = (width-pWidth)/2;
                oY = (height+pHeight-oHeight)/2;
                oX = (width - (oWidth * (numChildren-1)))/2;
                relativPosY=0;
            }
            for (var i:int = 1; i < numChildren; ++i) {
                item = getChildAt(i) as UserGraphicHolder;
                item.width = oWidth;
                item.height = oHeight;
                item.x=oX+(1-relativPosY)*(i-1)*oWidth
                item.y=oY+relativPosY*(i-1)*oHeight;
            }
        } 

        override public function validateDisplayList():void {
            super.validateDisplayList();

//            updateDisplayListHelper();
            updateDisplayListHelperByPriority2();
        }

        public function addAvatarFor(userId:String):void {
            if (! UsersUtil.hasUser(userId)) return;

            var graphic:UserGraphicHolder = new UserGraphicHolder();
            graphic.userId = userId;
            graphic.addEventListener(FlexEvent.CREATION_COMPLETE, function(event:FlexEvent):void {
                    graphic.loadAvatar(_options);
                    });
            super.addChild(graphic);
        }

        private function addVideoForHelper(userId:String, connection:NetConnection, streamName:String):void {
            trace("[GraphicsWrapper:addVideoForHelper] streamName " + streamName);
            var graphic:UserGraphicHolder = new UserGraphicHolder();
            graphic.userId = userId;
            graphic.addEventListener(FlexEvent.CREATION_COMPLETE, function(event:FlexEvent):void {
                    graphic.loadVideo(_options, connection, streamName);
                    });
            super.addChild(graphic);
        }

        private function getUserStreamNames(user:BBBUser):Array {
            if (user.streamName == null || user.streamName == "") {
                return new Array();
            } else {
                return user.streamName.split("|");
            }
        }

        public function addVideoFor(userId:String, connection:NetConnection):void {
            var user:BBBUser = UsersUtil.getUser(userId);
            if (user == null) return;

            var streamNames:Array = getUserStreamNames(user);

            for each (var streamName:String in streamNames) {
                if (user.viewingStream.indexOf(streamName) == -1) {
                    addVideoForHelper(user.userID, connection, streamName);
                }
            }
        }

        private function addCameraForHelper(userId:String, camIndex:int, videoProfile:VideoProfile):void {
            var graphic:UserGraphicHolder = new UserGraphicHolder();
            graphic.userId = userId;
            graphic.addEventListener(FlexEvent.CREATION_COMPLETE, function(event:FlexEvent):void {
                    graphic.loadCamera(_options, camIndex, videoProfile);
                    });
            super.addChild(graphic);
        }

        public function addCameraFor(userId:String, camIndex:int, videoProfile:VideoProfile):void {
            if (! UsersUtil.hasUser(userId)) return;

            var alreadyPublishing:Boolean = false;
            for (var i:int = 0; i < numChildren; ++i) {
                var item:UserGraphicHolder = getChildAt(i) as UserGraphicHolder;
                if (item.user && item.user.userID == userId && item.visibleComponent is UserVideo && item.video.camIndex == camIndex) {
                    alreadyPublishing = true;
                    break;
                }
            }

            if (!alreadyPublishing) {
                addCameraForHelper(userId, camIndex, videoProfile);
            }
        }

        private function removeChildHelper(child:UserGraphicHolder):void {
            child.shutdown();
            if (contains(child)) {
                removeChild(child);
            }
        }

        public function removeAvatarFor(userId:String):void {
            trace("[GraphicsWrapper:removeAvatarFor] userId " + userId);
            for (var i:int = 0; i < numChildren; ++i) {
                var item:UserGraphicHolder = getChildAt(i) as UserGraphicHolder;
                if (item.user && item.user.userID == userId && item.visibleComponent is UserAvatar) {
                    trace("[GraphicsWrapper:removeAvatarFor] removing graphic");
                    removeChildHelper(item);
                    // recursive call to remove all avatars for userId
                    removeAvatarFor(userId);
                    break;
                }
            }
        }

        public function removeVideoByCamIndex(userId:String, camIndex:int):String {
            trace("[GraphicsWrapper:removeVideoByCamIndex] userId " + userId + " camIndex " + camIndex);
            var streamName:String = "";

            for (var i:int = 0; i < numChildren; ++i) {
                var item:UserGraphicHolder = getChildAt(i) as UserGraphicHolder;
                if (item.user && item.user.userID == userId && item.visibleComponent is UserVideo && item.video.camIndex == camIndex) {
                    streamName = item.video.streamName;
                    removeChildHelper(item);
                    break;
                }
            }
            return streamName;
        }

        public function removeVideoByStreamName(userId:String, streamName:String):int {
            var camIndex:int = -1;

            for (var i:int = 0; i < numChildren; ++i) {
                var item:UserGraphicHolder = getChildAt(i) as UserGraphicHolder;
                if (item.user && item.user.userID == userId && item.visibleComponent is UserVideo && item.video.streamName == streamName) {
                    camIndex = item.video.camIndex;
                    removeChildHelper(item);
                    break;
                }
            }
            return camIndex;
        }

        public function removeGraphicsFor(userId:String):void {
            trace("[GraphicsWrapper:removeGraphicsFor] userId " + userId);
            for (var i:int = 0; i < numChildren; ++i) {
                var item:UserGraphicHolder = getChildAt(i) as UserGraphicHolder;
                if (item.user && item.user.userID == userId) {
                    trace("[GraphicsWrapper:removeGraphicsFor] removing graphic");
                    removeChildHelper(item);
                    // recursive call to remove all graphics for userId
                    removeGraphicsFor(userId);
                    break;
                }
            }
        }

        public function hasGraphicsFor(userId:String):Boolean {
            for (var i:int = 0; i < numChildren; ++i) {
                var item:UserGraphicHolder = getChildAt(i) as UserGraphicHolder;
                if (item.user && item.user.userID == userId) {
                    return true;
                }
            }
            return false;
        }

        public function shutdown():void {
            while (numChildren > 0) {
                var item:UserGraphicHolder = getChildAt(0) as UserGraphicHolder;
                removeChildHelper(item);
            }
        }
    }
}
