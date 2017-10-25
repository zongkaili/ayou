#include "AugmentedReality2DHandle.h"

int main(int argc, char *argv[])
{	
	std::vector<std::string> tempFiles({"/home/zhuochen/test.temp"});
	float fx = 629.504028f;
	float fy = 629.504028f;
	float cx = 320.f;
	float cy = 240.f;
	AugmentedReality2D_init(fx, fy, cx, cy);

	/* if do local recognition */
	/* 2017-02-17 14:27:06 Zhuo.Chen    for local recognition	begin */
	int id = AugmentedReality2D_localRecognition(imgDetect, tempFiles);
	if(id >= 0)
	{
		/* 2017-02-17 14:26:37 Zhuo.Chen    local recognition succed */
		/* 2017-02-17 14:31:52 Zhuo.Chen    id is the idx of result temp */
		AugmentedReality2D_loadFile(tempFiles[id]);
	}
	else
	{
		/* 2017-02-17 14:26:37 Zhuo.Chen    local recognition failed */
	}
	/* 2017-02-17 14:27:06 Zhuo.Chen    for local recognition	end */
	/* else if do server recognition */
	/* 2017-02-17 14:27:53 Zhuo.Chen    for server recognition	begin */
	/* some code */
	/* 2017-02-17 14:27:53 Zhuo.Chen    for server recognition	end */


	/* ar tracking loop */
	int res = AugmentedReality2D_run(imgDetect);
	/* ... */

	return 0;
}
