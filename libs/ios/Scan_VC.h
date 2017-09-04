#import <UIKit/UIKit.h>

@protocol ScanDelegate<NSObject>
-(void)scanSuccess:(NSString *)messStr;
-(void)scanfalse:(NSString *)messStr;
@end

@interface Scan_VC : UIViewController

@property (nonatomic,weak) id<ScanDelegate> delegate;

@end
